package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.SessionExerciseLogRequest;
import com.example.viegymapp.dto.response.SessionExerciseLogResponse;

import java.util.List;
import java.util.UUID;

public interface SessionExerciseLogService {
    SessionExerciseLogResponse createLog(SessionExerciseLogRequest request);

    List<SessionExerciseLogResponse> getLogBySessionId(UUID sessionId);

    SessionExerciseLogResponse updateLog(UUID id, SessionExerciseLogRequest request);

    void deleteLog(UUID id);

    boolean isLogOwner(UUID logId, String username);
}

