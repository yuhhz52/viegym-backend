package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.HealthLogRequest;
import com.example.viegymapp.dto.response.HealthLogResponse;
import com.example.viegymapp.entity.HealthLog;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.HealthLogMapper;
import com.example.viegymapp.repository.HealthLogRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.HealthLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthLogServiceImpl implements HealthLogService {
    private final HealthLogRepository healthLogRepo;
    private final UserRepository userRepo;
    private final HealthLogMapper mapper;

    @Override
    public List<HealthLogResponse> getHealthLogsOfCurrentUser() {
        User user = getCurrentUser();
        return healthLogRepo.findByUser(user).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HealthLogResponse createHealthLog(HealthLogRequest request) {
        User user = getCurrentUser();
        HealthLog log = mapper.toEntity(request);
        log.setUser(user);
        return mapper.toResponse(healthLogRepo.save(log));
    }

    @Override
    public HealthLogResponse updateHealthLog(UUID id, HealthLogRequest request) {
        HealthLog existing = healthLogRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_LOG_NOT_FOUND));

        mapper.updateEntity(existing, request);
        return mapper.toResponse(healthLogRepo.save(existing));
    }

    @Override
    public void deleteHealthLog(UUID id) {
        if (!healthLogRepo.existsById(id)) {
            throw new AppException(ErrorCode.HEALTH_LOG_NOT_FOUND);
        }
        healthLogRepo.deleteById(id);
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
