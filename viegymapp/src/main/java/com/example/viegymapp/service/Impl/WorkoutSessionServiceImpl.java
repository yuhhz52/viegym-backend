package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.WorkoutSessionRequest;
import com.example.viegymapp.dto.response.WorkoutSessionResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.WorkoutSession;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.WorkoutSessionMapper;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.repository.WorkoutProgramRepository;
import com.example.viegymapp.repository.WorkoutSessionRepository;
import com.example.viegymapp.service.WorkoutSessionService;
import com.example.viegymapp.service.StreakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionRepository sessionRepo;
    private final UserRepository userRepository;
    private final WorkoutProgramRepository programRepo;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final StreakService streakService;

    @Override
    public WorkoutSessionResponse createSession(WorkoutSessionRequest request) {
        var user = getCurrentUser();

        WorkoutSession session = workoutSessionMapper.toEntity(request);
        session.setUser(user);

        if (request.getProgramId() != null) {
            session.setProgram(programRepo.findById(request.getProgramId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROGRAM_NOT_FOUND)));
        }
        
        WorkoutSession savedSession = sessionRepo.save(session);
        // Tăng streak real-time khi tạo workout
        streakService.calculateAndUpdateStreak(user.getId());
        // Tính volume từ logs
        double totalVolume = calculateSessionVolume(savedSession);
        // Cập nhật stats: +1 workout và tổng volume
        Integer currentWorkouts = user.getTotalWorkouts() != null ? user.getTotalWorkouts() : 0;
        Double currentVolume = user.getTotalVolume() != null ? user.getTotalVolume() : 0.0;
        
        user.setTotalWorkouts(currentWorkouts + 1);
        user.setTotalVolume(currentVolume + totalVolume);
        userRepository.save(user);
        
        return workoutSessionMapper.toResponse(savedSession);
    }
    
    private double calculateSessionVolume(WorkoutSession session) {
        if (session.getLogs() == null || session.getLogs().isEmpty()) {
            return 0.0;
        }
        return session.getLogs().stream()
                .mapToDouble(log -> log.calculateVolume())
                .sum();
    }
    @Override
    public List<WorkoutSessionResponse> getAllSession() {
        var user = getCurrentUser();
        return sessionRepo.findByUserId(user.getId()).stream()
                .map(workoutSessionMapper::toResponse)
                .toList();
    }
    @Override
    public WorkoutSessionResponse getSessionById(UUID id) {
        return sessionRepo.findById(id)
                .map(workoutSessionMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
    }

    @Override
    public WorkoutSessionResponse updateSession(UUID id, WorkoutSessionRequest request) {
        WorkoutSession session = sessionRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        var user = getCurrentUser();
        
        // Lưu old session date để kiểm tra
        var oldSessionDate = session.getSessionDate();

        if (request.getSessionDate() != null) session.setSessionDate(request.getSessionDate());
        if (request.getDurationMinutes() != null) session.setDurationMinutes(request.getDurationMinutes());
        if (request.getNotes() != null) session.setNotes(request.getNotes());

        WorkoutSessionResponse response = workoutSessionMapper.toResponse(sessionRepo.save(session));
        
        // Nếu update sessionDate sang hôm nay → tính streak
        if (request.getSessionDate() != null && !oldSessionDate.equals(request.getSessionDate())) {
            streakService.calculateAndUpdateStreak(user.getId());
        }

        return response;
    }

    @Override
    public void deleteSession(UUID id) {
        var session = sessionRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        var user = getCurrentUser();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        try {
            // Tính volume của session trước khi xóa
            double volumeToRemove = calculateSessionVolume(session);
            
            // Xóa session (cascade sẽ tự động xóa logs)
            sessionRepo.deleteById(id);
            
            // Cập nhật stats: -1 workout và trừ volume (với null check)
            Integer currentWorkouts = user.getTotalWorkouts() != null ? user.getTotalWorkouts() : 0;
            Double currentVolume = user.getTotalVolume() != null ? user.getTotalVolume() : 0.0;
            
            user.setTotalWorkouts(Math.max(0, currentWorkouts - 1));
            user.setTotalVolume(Math.max(0.0, currentVolume - volumeToRemove));
            userRepository.save(user);
            
            // KHÔNG tính lại streak khi xóa session
            // Streak chỉ nên được tính khi TẠO hoặc CẬP NHẬT session
            
        } catch (Exception e) {
            throw new AppException(ErrorCode.DATABASE_ERROR);
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public boolean isSessionOwner(UUID sessionId, String username) {
        if (sessionId == null || username == null) {
            return false;
        }
        try {
            var session = sessionRepo.findById(sessionId);
            if (session.isEmpty()) {
                return false;
            }
            boolean isOwner = session.get().getUser().getEmail().equals(username);
            return isOwner;
        } catch (Exception e) {
            return false;
        }
    }
}
