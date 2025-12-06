package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.NutritionLogRequest;
import com.example.viegymapp.dto.response.NutritionLogResponse;
import com.example.viegymapp.entity.NutritionLog;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.NutritionLogMapper;
import com.example.viegymapp.repository.NutritionLogRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.NutritionLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NutritionLogServiceImpl implements NutritionLogService {
    private final NutritionLogRepository nutritionLogRepository;
    private final UserRepository userRepository;
    private final NutritionLogMapper nutritionLogMapper;

    @Override
    public NutritionLogResponse createLog(NutritionLogRequest request) {
        User user = getCurrentUser();

        NutritionLog log = nutritionLogMapper.toEntity(request);
        log.setUser(user);

        NutritionLog saved = nutritionLogRepository.save(log);
        return nutritionLogMapper.toResponse(saved);
    }

    @Override
    public List<NutritionLogResponse> getLogs(UUID userId, LocalDate date) {
        List<NutritionLog> logs = (date != null)
                ? nutritionLogRepository.findByUserIdAndLogDate(userId, date)
                : nutritionLogRepository.findByUserId(userId);

        return nutritionLogMapper.toResponseList(logs);
    }

    @Override
    public NutritionLogResponse updateLog(UUID id, NutritionLogRequest request) {
        NutritionLog log = nutritionLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOG_NOT_FOUND));

        nutritionLogMapper.updateEntityFromRequest(request, log);
        return nutritionLogMapper.toResponse(nutritionLogRepository.save(log));
    }

    @Override
    public void deleteLog(UUID id) {
        if (!nutritionLogRepository.existsById(id)) {
            throw new AppException(ErrorCode.LOG_NOT_FOUND);
        }
        nutritionLogRepository.deleteById(id);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
