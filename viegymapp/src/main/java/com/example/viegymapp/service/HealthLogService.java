package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.HealthLogRequest;
import com.example.viegymapp.dto.response.HealthLogResponse;
import com.example.viegymapp.entity.User;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

public interface HealthLogService {
    List<HealthLogResponse> getHealthLogsOfCurrentUser();
    HealthLogResponse createHealthLog(HealthLogRequest request);
    HealthLogResponse updateHealthLog(UUID id, HealthLogRequest request);
    void deleteHealthLog(UUID id);
}
