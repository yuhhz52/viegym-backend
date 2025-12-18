package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.AddClientRequest;
import com.example.viegymapp.dto.request.AssignProgramRequest;
import com.example.viegymapp.dto.request.WithdrawRequest;
import com.example.viegymapp.dto.response.ClientResponse;
import com.example.viegymapp.dto.response.CoachBalanceResponse;
import com.example.viegymapp.dto.response.CoachStatsResponse;
import com.example.viegymapp.dto.response.WorkoutProgramResponse;
import com.example.viegymapp.entity.*;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.WorkoutProgramMapper;
import com.example.viegymapp.repository.*;
import com.example.viegymapp.service.CoachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoachServiceImpl implements CoachService {

    private final CoachClientRepository coachClientRepository;
    private final UserRepository userRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final SavedProgramRepository savedProgramRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutProgramMapper workoutProgramMapper;
    private final BookingSessionRepository bookingRepository;
    private final com.example.viegymapp.repository.CoachBalanceRepository coachBalanceRepository;
    private final com.example.viegymapp.repository.CoachTransactionRepository coachTransactionRepository;
    private final com.example.viegymapp.service.CoachBalanceService coachBalanceService;

    /**
     * Get current coach from security context
     */
    private User getCurrentCoach() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User coach = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Log user roles for debugging
        log.debug("User {} has roles: {}", email, coach.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(java.util.stream.Collectors.toList()));
        
        // Verify user is a coach
        boolean isCoach = coach.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == com.example.viegymapp.entity.Enum.PredefinedRole.ROLE_COACH);
        
        if (!isCoach) {
            log.warn("User {} does not have ROLE_COACH. Current roles: {}", email, coach.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName().name())
                    .collect(java.util.stream.Collectors.toList()));
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return coach;
    }

    @Override
    public CoachStatsResponse getCoachStats() {
        User coach = getCurrentCoach();
        UUID coachId = coach.getId();
        
        // Calculate statistics
        long totalClients = coachClientRepository.countByCoachId(coachId);
        long activeClients = coachClientRepository.countByCoachIdAndStatus(coachId, "ACTIVE");
        
        // Count programs created by coach
        long totalPrograms = workoutProgramRepository.countByCreatorId(coachId);
        
        // Count workout sessions of all clients
        List<CoachClient> clients = coachClientRepository.findByCoachIdAndStatus(coachId, "ACTIVE");
        List<UUID> clientIds = clients.stream()
                .map(cc -> cc.getClient().getId())
                .collect(Collectors.toList());
        
        long totalWorkoutSessions = clientIds.isEmpty() ? 0 : 
                workoutSessionRepository.countByUserIdIn(clientIds);
        
        // Count new clients this month
        Instant startOfMonth = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .minus(30, ChronoUnit.DAYS);
        long newClientsThisMonth = coachClientRepository.countNewClientsSince(coachId, startOfMonth);
        
        // Count programs assigned to clients
        long activeProgramsAssigned = savedProgramRepository.countByUserIdIn(clientIds);
        
        // Calculate average client progress (simplified)
        double avgClientProgress = clientIds.isEmpty() ? 0.0 : 
                clients.stream()
                        .mapToDouble(cc -> cc.getClient().getTotalWorkouts())
                        .average()
                        .orElse(0.0);
        
        // Calculate booking statistics
        long totalBookings = bookingRepository.countByCoach(coach);
        long completedBookings = bookingRepository.countByCoachAndStatus(coach, BookingSession.BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByCoachAndStatus(coach, BookingSession.BookingStatus.CANCELLED);
        
        return CoachStatsResponse.builder()
                .totalClients((int) totalClients)
                .activeClients((int) activeClients)
                .totalPrograms((int) totalPrograms)
                .totalWorkoutsSessions((int) totalWorkoutSessions)
                .avgClientProgress(avgClientProgress)
                .newClientsThisMonth((int) newClientsThisMonth)
                .activeProgramsAssigned((int) activeProgramsAssigned)
                .totalBookings((int) totalBookings)
                .completedBookings((int) completedBookings)
                .cancelledBookings((int) cancelledBookings)
                .build();
    }

    @Override
    public List<ClientResponse> getMyClients() {
        User coach = getCurrentCoach();
        List<CoachClient> coachClients = coachClientRepository.findByCoachId(coach.getId());
        
        return coachClients.stream()
                .map(this::mapToClientResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientResponse> getActiveClients() {
        User coach = getCurrentCoach();
        List<CoachClient> coachClients = coachClientRepository
                .findByCoachIdAndStatus(coach.getId(), "ACTIVE");
        
        return coachClients.stream()
                .map(this::mapToClientResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponse getClientById(UUID clientId) {
        User coach = getCurrentCoach();
        CoachClient coachClient = coachClientRepository
                .findByCoachIdAndClientId(coach.getId(), clientId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENT_NOT_FOUND));
        
        return mapToClientResponse(coachClient);
    }

    @Override
    public ClientResponse addClient(AddClientRequest request) {
        User coach = getCurrentCoach();
        
        // Check if client exists
        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Check if relationship already exists
        if (coachClientRepository.existsByCoachIdAndClientId(coach.getId(), client.getId())) {
            throw new AppException(ErrorCode.CLIENT_ALREADY_EXISTS);
        }
        
        // Create coach-client relationship
        CoachClient coachClient = CoachClient.builder()
                .coach(coach)
                .client(client)
                .startedDate(Instant.now())
                .status("ACTIVE")
                .notes(request.getNotes())
                .build();
        
        coachClient = coachClientRepository.save(coachClient);
        
        return mapToClientResponse(coachClient);
    }

    @Override
    public void removeClient(UUID clientId) {
        User coach = getCurrentCoach();
        CoachClient coachClient = coachClientRepository
                .findByCoachIdAndClientId(coach.getId(), clientId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENT_NOT_FOUND));
        
        coachClientRepository.delete(coachClient);
    }

    @Override
    public ClientResponse updateClientNotes(UUID clientId, String notes) {
        User coach = getCurrentCoach();
        CoachClient coachClient = coachClientRepository
                .findByCoachIdAndClientId(coach.getId(), clientId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENT_NOT_FOUND));
        
        coachClient.setNotes(notes);
        coachClient = coachClientRepository.save(coachClient);
        
        return mapToClientResponse(coachClient);
    }

    @Override
    public List<WorkoutProgramResponse> getMyPrograms() {
        User coach = getCurrentCoach();
        List<WorkoutProgram> programs = workoutProgramRepository.findByCreatorId(coach.getId());
        
        return programs.stream()
                .map(workoutProgramMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void assignProgramToClient(AssignProgramRequest request) {
        User coach = getCurrentCoach();
        
        // Verify client belongs to this coach
        CoachClient coachClient = coachClientRepository
                .findByCoachIdAndClientId(coach.getId(), request.getClientId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENT_NOT_FOUND));
        
        // Verify program exists and belongs to coach
        WorkoutProgram program = workoutProgramRepository.findById(request.getProgramId())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));
        
        if (!program.getCreator().getId().equals(coach.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        User client = coachClient.getClient();
        
        // Check if program is already saved by client
        if (savedProgramRepository.existsByUserIdAndProgramId(client.getId(), program.getId())) {
            throw new AppException(ErrorCode.PROGRAM_ALREADY_SAVED);
        }
        
        // Save program for client
        SavedProgram savedProgram = SavedProgram.builder()
                .user(client)
                .program(program)
                .build();
        
        savedProgramRepository.save(savedProgram);
    }

    @Override
    public List<WorkoutProgramResponse> getClientPrograms(UUID clientId) {
        User coach = getCurrentCoach();
        
        // Verify client belongs to this coach
        coachClientRepository.findByCoachIdAndClientId(coach.getId(), clientId)
                .orElseThrow(() -> new AppException(ErrorCode.CLIENT_NOT_FOUND));
        
        // Get all saved programs for this client
        List<SavedProgram> savedPrograms = savedProgramRepository.findByUserId(clientId);
        
        return savedPrograms.stream()
                .map(sp -> workoutProgramMapper.toResponse(sp.getProgram()))
                .collect(Collectors.toList());
    }

    /**
     * Map CoachClient entity to ClientResponse DTO
     */
    private ClientResponse mapToClientResponse(CoachClient coachClient) {
        User client = coachClient.getClient();
        
        return ClientResponse.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .gender(client.getGender())
                .heightCm(client.getHeightCm())
                .weightKg(client.getWeightKg())
                .goal(client.getGoal())
                .experienceLevel(client.getExperienceLevel())
                .avatarUrl(client.getAvatarUrl())
                .totalWorkouts(client.getTotalWorkouts())
                .totalVolume(client.getTotalVolume())
                .streakDays(client.getStreakDays())
                .joinedDate(coachClient.getStartedDate())
                .status(coachClient.getStatus())
                .notes(coachClient.getNotes())
                .build();
    }

    @Override
    public CoachBalanceResponse getCoachBalance() {
        User coach = getCurrentCoach();
        
        // Initialize balance if not exists
        coachBalanceService.initializeCoachBalance(coach.getId());
        
        com.example.viegymapp.entity.CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        return CoachBalanceResponse.builder()
                .availableBalance(balance.getAvailableBalance())
                .pendingBalance(balance.getPendingBalance())
                .totalEarned(balance.getTotalEarned())
                .totalWithdrawn(balance.getTotalWithdrawn())
                .bankAccountInfo(balance.getBankAccountInfo())
                .lastUpdated(balance.getLastUpdated())
                .build();
    }

    @Override
    @Transactional
    public CoachBalanceResponse withdraw(WithdrawRequest request) {
        User coach = getCurrentCoach();
        
        // Initialize balance if not exists
        coachBalanceService.initializeCoachBalance(coach.getId());
        
        com.example.viegymapp.entity.CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Validate withdrawal amount
        if (request.getAmount().compareTo(balance.getAvailableBalance()) > 0) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }
        
        // Minimum withdrawal amount
        if (request.getAmount().compareTo(new java.math.BigDecimal("50000")) < 0) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }
        
        // Record balance before withdrawal
        BigDecimal balanceBefore = balance.getAvailableBalance();
        
        // Deduct from available balance
        balance.setAvailableBalance(balance.getAvailableBalance().subtract(request.getAmount()));
        balance.setTotalWithdrawn(balance.getTotalWithdrawn().add(request.getAmount()));
        balance.setBankAccountInfo(request.getBankAccountInfo());
        balance.setLastUpdated(java.time.LocalDateTime.now());
        
        coachBalanceRepository.save(balance);
        
        // Create withdrawal transaction record
        CoachTransaction withdrawalTransaction = CoachTransaction.builder()
                .coach(coach)
                .type(CoachTransaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .platformFee(java.math.BigDecimal.ZERO) // No platform fee for withdrawal
                .netAmount(request.getAmount().negate()) // Negative because it's a withdrawal
                .balanceBefore(balanceBefore)
                .balanceAfter(balance.getAvailableBalance())
                .status(CoachTransaction.TransactionStatus.PENDING) // PENDING until processed by admin
                .description(String.format("Yêu cầu rút tiền - %s", request.getBankAccountInfo()))
                .processedAt(java.time.LocalDateTime.now())
                .build();
        
        coachTransactionRepository.save(withdrawalTransaction);
        
        log.info("Coach {} withdrew {} VNĐ. New balance: {}. Transaction ID: {}", 
                coach.getId(), request.getAmount(), balance.getAvailableBalance(), withdrawalTransaction.getId());
        
        return CoachBalanceResponse.builder()
                .availableBalance(balance.getAvailableBalance())
                .pendingBalance(balance.getPendingBalance())
                .totalEarned(balance.getTotalEarned())
                .totalWithdrawn(balance.getTotalWithdrawn())
                .bankAccountInfo(balance.getBankAccountInfo())
                .lastUpdated(balance.getLastUpdated())
                .build();
    }

    @Override
    @Transactional
    public int processPendingCompletedBookings() {
        User coach = getCurrentCoach();
        return coachBalanceService.processPendingCompletedBookings(coach.getId());
    }
}
