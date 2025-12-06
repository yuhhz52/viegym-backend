package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.ProgramExerciseRequest;
import com.example.viegymapp.dto.request.ProgramRatingRequest;
import com.example.viegymapp.dto.request.WorkoutProgramRequest;
import com.example.viegymapp.dto.response.ProgramExerciseResponse;
import com.example.viegymapp.dto.response.ProgramRatingResponse;
import com.example.viegymapp.dto.response.ProgramStatsResponse;
import com.example.viegymapp.dto.response.WorkoutProgramResponse;
import com.example.viegymapp.entity.*;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.ProgramExerciseMapper;
import com.example.viegymapp.mapper.WorkoutProgramMapper;
import com.example.viegymapp.repository.*;
import com.example.viegymapp.service.AsyncNotificationService;
import com.example.viegymapp.service.WorkoutProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutProgramServiceImpl implements WorkoutProgramService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WorkoutProgramServiceImpl.class);

    private final WorkoutProgramRepository programRepo;
    private final ProgramExerciseRepository programExerciseRepo;
    private final ExerciseRepository exerciseRepo;
    private final UserRepository userRepository;
    private final WorkoutProgramMapper programMapper;
    private final ProgramExerciseMapper programExerciseMapper;
    private final ProgramMediaRepository programMediaRepo;
    private final ProgramRatingRepository ratingRepo;
    private final SavedProgramRepository savedProgramRepo;
    private final AsyncNotificationService asyncNotificationService;


    @Override
    public List<WorkoutProgramResponse> getAllPrograms() {
        try {
            var user = getCurrentUser();
            // Return user's programs + public programs from others
            return programRepo.findUserAndPublicPrograms(user.getId()).stream()
                    .map(programMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // User not authenticated, return only public programs
            return programRepo.findAllPublicPrograms().stream()
                    .map(programMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<WorkoutProgramResponse> getAdminPrograms() {
        var user = getCurrentUser();
        var roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());
        
        log.info("getAdminPrograms - User ID: {}, Roles: {}", user.getId(), roles);
        
        // SUPER_ADMIN: Xem tất cả chương trình của ADMIN và SUPER_ADMIN (không xem COACH)
        if (roles.contains("ROLE_SUPER_ADMIN")) {
            return programRepo.findAll().stream()
                    .filter(program -> {
                        if (program.getCreator() == null) return false;
                        var creatorRoles = program.getCreator().getUserRoles().stream()
                                .map(UserRole::getRole)
                                .map(Role::getName)
                                .map(Enum::name)
                                .collect(Collectors.toSet());
                        // Chỉ lấy programs của ADMIN hoặc SUPER_ADMIN
                        return creatorRoles.contains("ROLE_ADMIN") || creatorRoles.contains("ROLE_SUPER_ADMIN");
                    })
                    .map(programMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        // ADMIN: Xem chương trình của chính mình + chương trình không có creator (legacy)
        if (roles.contains("ROLE_ADMIN")) {
            var allPrograms = programRepo.findAll();
            log.info("ADMIN filter - Total programs: {}", allPrograms.size());
            
            var filtered = allPrograms.stream()
                    .filter(program -> {
                        // Lấy chương trình của chính mình
                        if (program.getCreator() != null && program.getCreator().getId().equals(user.getId())) {
                            log.info("Program {} matched - created by current user", program.getId());
                            return true;
                        }
                        // Lấy chương trình legacy không có creator
                        if (program.getCreator() == null) {
                            log.info("Program {} matched - legacy (no creator)", program.getId());
                            return true;
                        }
                        log.info("Program {} filtered out - creator: {}", program.getId(), 
                                program.getCreator() != null ? program.getCreator().getId() : "null");
                        return false;
                    })
                    .map(programMapper::toResponse)
                    .collect(Collectors.toList());
            
            log.info("ADMIN filter result - Returning {} programs", filtered.size());
            return filtered;
        }
        
        // COACH: Chỉ xem chương trình của chính mình
        if (roles.contains("ROLE_COACH")) {
            var programs = programRepo.findByCreatorId(user.getId());
            log.info("COACH filter - Found {} programs for user {}", programs.size(), user.getId());
            return programs.stream()
                    .map(programMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        // USER: Không truy cập được endpoint này
        log.warn("No matching role for user {} with roles {} - returning empty list", user.getId(), roles);
        return List.of();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public WorkoutProgramResponse getProgramById(UUID id) {
        return programRepo.findById(id)
                .map(programMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));
    }

    @Override
    public WorkoutProgramResponse createProgram(WorkoutProgramRequest request) {
        var user = getCurrentUser();
        
        WorkoutProgram program = WorkoutProgram.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .goal(request.getGoal())
                .durationWeeks(request.getDurationWeeks())
                .visibility(request.getVisibility())
                .creator(user)
                .build();

        WorkoutProgram saved = programRepo.save(program);

        if (request.getMediaList() != null) {
            List<ProgramMedia> media = request.getMediaList().stream()
                    .map(m -> ProgramMedia.builder()
                            .url(m.getUrl())
                            .mediaType(m.getMediaType())
                            .program(saved)
                            .build())
                    .collect(Collectors.toList());
            programMediaRepo.saveAll(media);
            saved.setMediaList(media);
        }
        return programMapper.toResponse(saved);
    }

    @Override
    public WorkoutProgramResponse createUserProgram(WorkoutProgramRequest request) {
        var user = getCurrentUser();
        
        WorkoutProgram program = WorkoutProgram.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .goal(request.getGoal())
                .durationWeeks(request.getDurationWeeks())
                .visibility("private")  // User programs are always private
                .creator(user)
                .build();

        WorkoutProgram saved = programRepo.save(program);

        if (request.getMediaList() != null) {
            List<ProgramMedia> media = request.getMediaList().stream()
                    .map(m -> ProgramMedia.builder()
                            .url(m.getUrl())
                            .mediaType(m.getMediaType())
                            .program(saved)
                            .build())
                    .collect(Collectors.toList());
            programMediaRepo.saveAll(media);
            saved.setMediaList(media);
        }
        return programMapper.toResponse(saved);
    }

    @Override
    public WorkoutProgramResponse updateProgram(UUID id, WorkoutProgramRequest request) {
        WorkoutProgram existing = programRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));

        WorkoutProgram updated = existing.toBuilder()
                .title(request.getTitle())
                .description(request.getDescription())
                .goal(request.getGoal())
                .durationWeeks(request.getDurationWeeks())
                .visibility(request.getVisibility())
                .build();

        WorkoutProgram saved = programRepo.save(updated);

        // Update media list if provided
        if (request.getMediaList() != null) {
            // Delete existing media
            programMediaRepo.deleteByProgramId(id);
            
            // Add new media
            List<ProgramMedia> media = request.getMediaList().stream()
                    .map(m -> ProgramMedia.builder()
                            .url(m.getUrl())
                            .mediaType(m.getMediaType())
                            .program(saved)
                            .build())
                    .collect(Collectors.toList());
            programMediaRepo.saveAll(media);
            saved.setMediaList(media);
        }

        return programMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public void deleteProgram(UUID id) {
        if (!programRepo.existsById(id)) {
            throw new AppException(ErrorCode.PROGRAM_NOT_FOUND);
        }
        
        // Delete related data first to avoid foreign key constraint violations
        programExerciseRepo.deleteByProgramId(id);
        programMediaRepo.deleteByProgramId(id);
        ratingRepo.deleteByProgramId(id);
        savedProgramRepo.deleteByProgramId(id);
        
        // Now delete the program
        programRepo.deleteById(id);
    }

    @Override
    public List<ProgramExerciseResponse> getExercisesInProgram(UUID programId) {
        return programExerciseRepo.findByProgramId(programId).stream()
                .map(programExerciseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProgramExerciseResponse addExerciseToProgram(UUID programId, ProgramExerciseRequest request) {
        WorkoutProgram program = programRepo.findById(programId)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));
        Exercise exercise = exerciseRepo.findById(UUID.fromString(request.getExerciseId()))
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        ProgramExercise pe = ProgramExercise.builder()
                .program(program)
                .exercise(exercise)
                .dayOfProgram(request.getDayOfProgram())
                .orderNo(request.getOrderNo())
                .sets(request.getSets())
                .reps(request.getReps())
                .weightScheme(request.getWeightScheme())
                .restSeconds(request.getRestSeconds())
                .notes(request.getNotes())
                .build();

        return programExerciseMapper.toResponse(programExerciseRepo.save(pe));
    }

    @Override
    public ProgramExerciseResponse updateProgramExercise(UUID programExerciseId, ProgramExerciseRequest request) {
        ProgramExercise existing = programExerciseRepo.findById(programExerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_EXERCISE_NOT_FOUND));

        ProgramExercise updated = existing.toBuilder()
                .dayOfProgram(request.getDayOfProgram())
                .orderNo(request.getOrderNo())
                .sets(request.getSets())
                .reps(request.getReps())
                .weightScheme(request.getWeightScheme())
                .restSeconds(request.getRestSeconds())
                .notes(request.getNotes())
                .build();

        return programExerciseMapper.toResponse(programExerciseRepo.save(updated));
    }

    @Override
    public void deleteProgramExercise(UUID programExerciseId) {
        if (!programExerciseRepo.existsById(programExerciseId)) {
            throw new AppException(ErrorCode.PROGRAM_EXERCISE_NOT_FOUND);
        }
        programExerciseRepo.deleteById(programExerciseId);
    }

    @Override
    public List<WorkoutProgramResponse> getPopularPrograms(int limit) {
        List<Object[]> popularProgramIds = ratingRepo.findProgramsByRatingCount();
        
        return popularProgramIds.stream()
                .limit(limit)
                .map(row -> (UUID) row[0])
                .map(programRepo::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> programMapper.toResponse(opt.get()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutProgramResponse> getSavedPrograms(UUID userId) {
        List<SavedProgram> savedPrograms = savedProgramRepo.findByUserId(userId);
        return savedPrograms.stream()
                .map(sp -> programMapper.toResponse(sp.getProgram()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProgramRatingResponse rateProgram(UUID programId, UUID userId, ProgramRatingRequest request) {
        WorkoutProgram program = programRepo.findById(programId)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if user already rated this program
        ProgramRating rating = ratingRepo.findByProgramIdAndUserId(programId, userId)
                .orElse(ProgramRating.builder()
                        .program(program)
                        .user(user)
                        .build());

        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        ProgramRating saved = ratingRepo.save(rating);
        
        // Refresh to get auto-generated timestamps
        saved = ratingRepo.findById(saved.getId()).orElse(saved);

        return ProgramRatingResponse.builder()
                .id(saved.getId())
                .programId(programId)
                .userId(userId)
                .userName(user.getFullName())
                .rating(saved.getRating())
                .review(saved.getReview())
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt().toInstant() : java.time.Instant.now())
                .build();
    }

    @Override
    public List<ProgramRatingResponse> getProgramRatings(UUID programId) {
        return ratingRepo.findByProgramId(programId).stream()
                .map(r -> ProgramRatingResponse.builder()
                        .id(r.getId())
                        .programId(programId)
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getFullName())
                        .rating(r.getRating())
                        .review(r.getReview())
                        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : java.time.Instant.now())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ProgramStatsResponse getProgramStats(UUID programId, UUID userId) {
        if (!programRepo.existsById(programId)) {
            throw new AppException(ErrorCode.PROGRAM_NOT_FOUND);
        }

        Double avgRating = ratingRepo.getAverageRating(programId);
        Long totalRatings = ratingRepo.getRatingCount(programId);
        
        // Handle null userId (unauthenticated user)
        boolean isSaved = false;
        Integer userRating = null;
        
        if (userId != null) {
            isSaved = savedProgramRepo.existsByProgramIdAndUserId(programId, userId);
            userRating = ratingRepo.findByProgramIdAndUserId(programId, userId)
                    .map(ProgramRating::getRating)
                    .orElse(null);
        }

        return ProgramStatsResponse.builder()
                .programId(programId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .totalSaves(0L) // Can add count later if needed
                .isSaved(isSaved)
                .userRating(userRating)
                .build();
    }

    @Override
    @Transactional
    public void saveProgram(UUID programId, UUID userId) {
        if (savedProgramRepo.existsByProgramIdAndUserId(programId, userId)) {
            return; // Already saved
        }

        WorkoutProgram program = programRepo.findById(programId)
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        SavedProgram savedProgram = SavedProgram.builder()
                .program(program)
                .user(user)
                .build();

        savedProgramRepo.save(savedProgram);
        
        // Send notification to user about new program - ASYNC via RabbitMQ
        asyncNotificationService.publishProgramNotification(
            userId,
            program.getCreator() != null ? program.getCreator().getFullName() : "Hệ thống",
            program.getTitle()
        );
    }

    @Override
    @Transactional
    public void unsaveProgram(UUID programId, UUID userId) {
        savedProgramRepo.deleteByProgramIdAndUserId(programId, userId);
    }

    @Override
    public boolean isProgramSaved(UUID programId, UUID userId) {
        return savedProgramRepo.existsByProgramIdAndUserId(programId, userId);
    }
}
