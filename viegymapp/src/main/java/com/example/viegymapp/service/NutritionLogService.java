package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.NutritionLogRequest;
import com.example.viegymapp.dto.response.NutritionLogResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NutritionLogService {
    NutritionLogResponse createLog(NutritionLogRequest request);

    List<NutritionLogResponse> getLogs(UUID userId, LocalDate date);

    NutritionLogResponse updateLog(UUID id, NutritionLogRequest request);

    void deleteLog(UUID id);
}
