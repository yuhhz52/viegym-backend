package com.example.viegymapp.service;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.ExerciseRequest;
import com.example.viegymapp.dto.request.ExerciseMediaRequest;
import com.example.viegymapp.dto.response.ExerciseMediaResponse;
import com.example.viegymapp.dto.response.ExerciseResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ExerciseService {

    PagingResponse<ExerciseResponse> getExercises(String tag, String difficulty, String muscleGroup, int page, int size);

    ExerciseResponse getExerciseById(UUID id);

    ExerciseResponse createExercise(ExerciseRequest createRequest);

    ExerciseResponse updateExercise(UUID id, ExerciseRequest updateRequest);

    void deleteExercise(UUID id);

    List<ExerciseMediaResponse> getMedia(UUID exerciseId);

    ExerciseMediaResponse addMedia(UUID exerciseId, ExerciseMediaRequest mediaCreateRequest);

    void deleteMedia(UUID mediaId);

    List<ExerciseResponse> getAllExercises();

}
