package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.*;
import com.example.viegymapp.dto.response.*;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.WorkoutProgramService;
import com.example.viegymapp.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/programs")
public class WorkoutProgramController {

    @Autowired
    private WorkoutProgramService programService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<List<WorkoutProgramResponse>> getAllPrograms() {
        return ApiResponse.<List<WorkoutProgramResponse>>builder()
                .result(programService.getAllPrograms())
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<WorkoutProgramResponse >> listPrograms() {
        return ApiResponse.<List<WorkoutProgramResponse >>builder()
                .result(programService.getAdminPrograms())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<WorkoutProgramResponse> getProgram(@PathVariable UUID id) {
        return ApiResponse.<WorkoutProgramResponse>builder()
                .result(programService.getProgramById(id))
                .build();
    }

    @PostMapping("/my/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<WorkoutProgramResponse> createMyProgram(@RequestBody WorkoutProgramRequest request) {
        return ApiResponse.<WorkoutProgramResponse>builder()
                .result(programService.createUserProgram(request))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<WorkoutProgramResponse> createProgram(@RequestBody WorkoutProgramRequest request) {
        return ApiResponse.<WorkoutProgramResponse>builder()
                .result(programService.createProgram(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<WorkoutProgramResponse> updateProgram(@PathVariable UUID id,
                                                             @RequestBody WorkoutProgramRequest request) {
        return ApiResponse.<WorkoutProgramResponse>builder()
                .result(programService.updateProgram(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UUID> deleteProgram(@PathVariable UUID id) {
        programService.deleteProgram(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    @GetMapping("/{id}/exercises")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<ProgramExerciseResponse>> getExercises(@PathVariable UUID id) {
        return ApiResponse.<List<ProgramExerciseResponse>>builder()
                .result(programService.getExercisesInProgram(id))
                .build();
    }

    @PostMapping("/{id}/exercises")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<ProgramExerciseResponse> addExercise(@PathVariable UUID id,
                                                            @RequestBody ProgramExerciseRequest request) {
        return ApiResponse.<ProgramExerciseResponse>builder()
                .result(programService.addExerciseToProgram(id, request))
                .build();
    }

    @PutMapping("/exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<ProgramExerciseResponse> updateExercise(@PathVariable UUID id,
                                                               @RequestBody ProgramExerciseRequest request) {
        return ApiResponse.<ProgramExerciseResponse>builder()
                .result(programService.updateProgramExercise(id, request))
                .build();
    }

    @DeleteMapping("/exercises/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UUID> deleteExercise(@PathVariable UUID id) {
        programService.deleteProgramExercise(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    @GetMapping("/popular")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<WorkoutProgramResponse>> getPopularPrograms(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<WorkoutProgramResponse>>builder()
                .result(programService.getPopularPrograms(limit))
                .build();
    }

    // Rating endpoints
    @PostMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<ProgramRatingResponse> rateProgram(@PathVariable UUID id,
                                                          @RequestBody @jakarta.validation.Valid ProgramRatingRequest request) {
        var user = getCurrentUser();
        return ApiResponse.<ProgramRatingResponse>builder()
                .result(programService.rateProgram(id, user.getId(), request))
                .build();
    }

    @GetMapping("/{id}/ratings")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<ProgramRatingResponse>> getProgramRatings(@PathVariable UUID id) {
        return ApiResponse.<List<ProgramRatingResponse>>builder()
                .result(programService.getProgramRatings(id))
                .build();
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("permitAll()")
    public ApiResponse<ProgramStatsResponse> getProgramStats(@PathVariable UUID id) {
        UUID userId = null;
        try {
            var user = getCurrentUser();
            userId = user.getId();
        } catch (Exception e) {
        }
        return ApiResponse.<ProgramStatsResponse>builder()
                .result(programService.getProgramStats(id, userId))
                .build();
    }

    @PostMapping("/{id}/save")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<String> saveProgram(@PathVariable UUID id) {
        var user = getCurrentUser();
        programService.saveProgram(id, user.getId());
        return ApiResponse.<String>builder()
                .result("Program saved successfully")
                .build();
    }

    @DeleteMapping("/{id}/save")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<String> unsaveProgram(@PathVariable UUID id) {
        var user = getCurrentUser();
        programService.unsaveProgram(id, user.getId());
        return ApiResponse.<String>builder()
                .result("Program unsaved successfully")
                .build();
    }

    @GetMapping("/saved")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<WorkoutProgramResponse>> getSavedPrograms() {
        var user = getCurrentUser();
        return ApiResponse.<List<WorkoutProgramResponse>>builder()
                .result(programService.getSavedPrograms(user.getId()))
                .build();
    }
}
