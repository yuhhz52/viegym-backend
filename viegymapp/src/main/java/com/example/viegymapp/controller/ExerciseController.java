package com.example.viegymapp.controller;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.ExerciseMediaRequest;
import com.example.viegymapp.dto.request.ExerciseRequest;
import com.example.viegymapp.dto.response.*;
import com.example.viegymapp.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<PagingResponse<ExerciseResponse>> getExercises(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagingResponse<ExerciseResponse> result =
                exerciseService.getExercises(tag, difficulty, muscleGroup, page, size);

        return ApiResponse.<PagingResponse<ExerciseResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<ExerciseResponse>> listExercises() {
        return ApiResponse.<List<ExerciseResponse>>builder()
                .result(exerciseService.getAllExercises())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<ExerciseResponse> getExerciseById(@PathVariable UUID id) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.getExerciseById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<ExerciseResponse> createExercise(@RequestBody ExerciseRequest createRequest) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.createExercise(createRequest))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ExerciseResponse> updateExercise(@PathVariable UUID id,
                                                        @RequestBody ExerciseRequest updateRequest) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.updateExercise(id, updateRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UUID> deleteExercise(@PathVariable UUID id) {
        exerciseService.deleteExercise(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    @GetMapping("/{exerciseId}/media")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<ExerciseMediaResponse>> getMedia(@PathVariable UUID exerciseId) {
        return ApiResponse.<List<ExerciseMediaResponse>>builder()
                .result(exerciseService.getMedia(exerciseId))
                .build();
    }

    @PostMapping("/{exerciseId}/media")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<ExerciseMediaResponse> addMedia(@PathVariable UUID exerciseId,
                                                       @RequestBody ExerciseMediaRequest mediaCreateRequest) {
        return ApiResponse.<ExerciseMediaResponse>builder()
                .result(exerciseService.addMedia(exerciseId, mediaCreateRequest))
                .build();
    }

    @DeleteMapping("/media/{mediaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UUID> deleteMedia(@PathVariable UUID mediaId) {
        exerciseService.deleteMedia(mediaId);
        return ApiResponse.<UUID>builder()
                .result(mediaId)
                .build();
    }
}
