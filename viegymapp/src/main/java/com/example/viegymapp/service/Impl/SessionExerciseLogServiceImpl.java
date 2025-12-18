package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.SessionExerciseLogRequest;
import com.example.viegymapp.dto.response.SessionExerciseLogResponse;
import com.example.viegymapp.entity.SessionExerciseLog;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.SessionExerciseLogMapper;
import com.example.viegymapp.repository.ExerciseRepository;
import com.example.viegymapp.repository.SessionExerciseLogRepository;
import com.example.viegymapp.repository.WorkoutSessionRepository;
import com.example.viegymapp.service.SessionExerciseLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionExerciseLogServiceImpl implements SessionExerciseLogService {
    private final SessionExerciseLogRepository logRepo;
    private final WorkoutSessionRepository sessionRepo;
    private final ExerciseRepository exerciseRepo;
    private final SessionExerciseLogMapper sessionExerciseLogMapper;

    @Override
    public SessionExerciseLogResponse createLog(SessionExerciseLogRequest request) {
        SessionExerciseLog sessionLog = sessionExerciseLogMapper.toEntity(request);
        
        var session = sessionRepo.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        sessionLog.setSession(session);
        
        sessionLog.setExercise(exerciseRepo.findById(request.getExerciseId())
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND)));
        
        // Set default completed = false if not provided
        if (sessionLog.getCompleted() == null) {
            sessionLog.setCompleted(false);
        }
        
        SessionExerciseLog savedLog = logRepo.save(sessionLog);
        
        // Cập nhật totalVolume của user
        double volumeAdded = calculateLogVolume(savedLog);
        var user = session.getUser();
        user.setTotalVolume(user.getTotalVolume() + volumeAdded);
        
        return sessionExerciseLogMapper.toResponse(savedLog);
    }
    
    private double calculateLogVolume(SessionExerciseLog log) {
        return log.calculateVolume();
    }

    @Override
    public List<SessionExerciseLogResponse> getLogBySessionId(UUID sessionId) {
        return logRepo.findBySessionId(sessionId)
                .stream().map(sessionExerciseLogMapper::toResponse).toList();
    }

    @Override
    public SessionExerciseLogResponse updateLog(UUID id, SessionExerciseLogRequest request) {
        SessionExerciseLog log = logRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOG_NOT_FOUND));

        // Tính volume cũ trước khi update
        double oldVolume = calculateLogVolume(log);
        
        // Update tất cả fields
        if (request.getSetNumber() != null) log.setSetNumber(request.getSetNumber());
        if (request.getRepsDone() != null) log.setRepsDone(request.getRepsDone());
        if (request.getWeightUsed() != null) log.setWeightUsed(request.getWeightUsed());
        if (request.getDurationSeconds() != null) log.setDurationSeconds(request.getDurationSeconds());
        if (request.getDistanceMeters() != null) log.setDistanceMeters(request.getDistanceMeters());
        if (request.getBodyWeight() != null) log.setBodyWeight(request.getBodyWeight());
        if (request.getSetNotes() != null) log.setSetNotes(request.getSetNotes());
        if (request.getCompleted() != null) log.setCompleted(request.getCompleted());
        
        SessionExerciseLog updatedLog = logRepo.save(log);
        
        // Tính volume mới và cập nhật user
        double newVolume = calculateLogVolume(updatedLog);
        double volumeDiff = newVolume - oldVolume;
        
        var user = log.getSession().getUser();
        user.setTotalVolume(Math.max(0.0, user.getTotalVolume() + volumeDiff));

        return sessionExerciseLogMapper.toResponse(updatedLog);
    }

    @Override
    public void deleteLog(UUID id) {
        SessionExerciseLog log = logRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOG_NOT_FOUND));
        
        // Kiểm tra log đã bị xóa chưa (soft delete)
        if (log.getDeleted() != null && log.getDeleted()) {
            throw new AppException(ErrorCode.LOG_NOT_FOUND);
        }
        
        // Kiểm tra session có tồn tại và chưa bị xóa không
        var session = log.getSession();
        if (session == null || (session.getDeleted() != null && session.getDeleted())) {
            throw new AppException(ErrorCode.SESSION_NOT_FOUND);
        }
        
        // Tính volume trước khi xóa
        double volumeToRemove = calculateLogVolume(log);
        
        // Cập nhật user totalVolume
        var user = session.getUser();
        if (user != null) {
            double currentVolume = user.getTotalVolume() != null ? user.getTotalVolume() : 0.0;
            user.setTotalVolume(Math.max(0.0, currentVolume - volumeToRemove));
        }
        
        logRepo.deleteById(id);
    }

    @Override
    public boolean isLogOwner(UUID logId, String username) {
        if (logId == null || username == null) {
            return false;
        }
        try {
            return logRepo.findById(logId)
                    .map(log -> {
                        // Kiểm tra log chưa bị xóa
                        if (log.getDeleted() != null && log.getDeleted()) {
                            return false;
                        }
                        // Kiểm tra session tồn tại và chưa bị xóa
                        var session = log.getSession();
                        if (session == null || (session.getDeleted() != null && session.getDeleted())) {
                            return false;
                        }
                        // Kiểm tra user tồn tại
                        var user = session.getUser();
                        return user != null && username.equals(user.getEmail());
                    })
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
