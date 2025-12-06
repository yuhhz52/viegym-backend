package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.WorkoutSessionRequest;
import com.example.viegymapp.dto.response.WorkoutSessionResponse;

import java.util.List;
import java.util.UUID;

public interface WorkoutSessionService {

    WorkoutSessionResponse createSession(WorkoutSessionRequest request);

    List<WorkoutSessionResponse> getAllSession();

    WorkoutSessionResponse getSessionById(UUID id);

    WorkoutSessionResponse updateSession(UUID id, WorkoutSessionRequest request);

    void deleteSession(UUID id);

    boolean isSessionOwner(UUID sessionId, String username);

}
