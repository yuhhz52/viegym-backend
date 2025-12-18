package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.SessionExerciseLogRequest;
import com.example.viegymapp.dto.request.WorkoutSessionRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.SessionExerciseLogResponse;
import com.example.viegymapp.dto.response.WorkoutSessionResponse;
import com.example.viegymapp.service.SessionExerciseLogService;
import com.example.viegymapp.service.WorkoutSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutSessionController {
    @Autowired
    private WorkoutSessionService sessionService;
    @Autowired
    private SessionExerciseLogService logService;

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN')")
    public ApiResponse<WorkoutSessionResponse> createSession(@RequestBody WorkoutSessionRequest req) {
        return ApiResponse.<WorkoutSessionResponse>builder()
                .result(sessionService.createSession(req))
                .build();
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<WorkoutSessionResponse>> getAllSession() {
        return ApiResponse.<List<WorkoutSessionResponse>>builder()
                .result(sessionService.getAllSession())
                .build();
    }

    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN') and @workoutSessionServiceImpl.isSessionOwner(#id, authentication.name)")
    public ApiResponse<WorkoutSessionResponse> getSessionById(@PathVariable UUID id) {
        return ApiResponse.<WorkoutSessionResponse>builder()
                .result(sessionService.getSessionById(id))
                .build();
    }

    // --- Cập nhật session ---
    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN', 'SUPER_ADMIN') and (@workoutSessionServiceImpl.isSessionOwner(#id, authentication.name) or hasAnyRole('ADMIN', 'SUPER_ADMIN'))")
    public ApiResponse<WorkoutSessionResponse> updateSession(@PathVariable UUID id,
                                                             @RequestBody WorkoutSessionRequest req) {
        return ApiResponse.<WorkoutSessionResponse>builder()
                .result(sessionService.updateSession(id, req))
                .build();
    }

    // --- Xóa session ---
    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN', 'SUPER_ADMIN') and (@workoutSessionServiceImpl.isSessionOwner(#id, authentication.name) or hasAnyRole('ADMIN', 'SUPER_ADMIN'))")
    public ApiResponse<UUID> delete(@PathVariable UUID id) {
        sessionService.deleteSession(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    // --- Logs ---
    @GetMapping("/sessions/{sessionId}/logs")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN') and @workoutSessionServiceImpl.isSessionOwner(#sessionId, authentication.name)")
    public ApiResponse<List<SessionExerciseLogResponse>> getLogsBySession(@PathVariable UUID sessionId) {
        return ApiResponse.<List<SessionExerciseLogResponse>>builder()
                .result(logService.getLogBySessionId(sessionId))
                .build();
    }

    @PostMapping("/sessions/{sessionId}/logs")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN') and @workoutSessionServiceImpl.isSessionOwner(#sessionId, authentication.name)")
    public ApiResponse<SessionExerciseLogResponse> createLog(@PathVariable UUID sessionId,
                                                             @Valid @RequestBody SessionExerciseLogRequest request) {
        request.setSessionId(sessionId);
        return ApiResponse.<SessionExerciseLogResponse>builder()
                .result(logService.createLog(request))
                .build();
    }

    @PutMapping("/logs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @sessionExerciseLogServiceImpl.isLogOwner(#id, authentication.name)")
    public ApiResponse<SessionExerciseLogResponse> updateLog(@PathVariable UUID id,
                                                             @Valid @RequestBody SessionExerciseLogRequest request) {
        return ApiResponse.<SessionExerciseLogResponse>builder()
                .result(logService.updateLog(id, request))
                .build();
    }

    @DeleteMapping("/logs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @sessionExerciseLogServiceImpl.isLogOwner(#id, authentication.name)")
    public ApiResponse<UUID> deleteLog(@PathVariable UUID id) {
        logService.deleteLog(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    // ============ TEST ENDPOINT ============
    // Create workout session for today (for testing streak calculation)
    @PostMapping("/sessions/test/today")
    @PreAuthorize("hasAnyRole('USER','COACH','ADMIN','SUPER_ADMIN')")
    public ApiResponse<WorkoutSessionResponse> createTestSessionToday() {
        WorkoutSessionRequest req = new WorkoutSessionRequest();
        req.setSessionDate(Instant.now()); // Hôm nay
        req.setDurationMinutes(60);
        req.setNotes("Test workout for today - testing streak calculation");
        return ApiResponse.<WorkoutSessionResponse>builder()
                .result(sessionService.createSession(req))
                .build();
    }
}
