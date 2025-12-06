package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.WorkoutProgramRequest;
import com.example.viegymapp.dto.request.ProgramExerciseRequest;
import com.example.viegymapp.dto.request.ProgramRatingRequest;
import com.example.viegymapp.dto.response.WorkoutProgramResponse;
import com.example.viegymapp.dto.response.ProgramExerciseResponse;
import com.example.viegymapp.dto.response.ProgramRatingResponse;
import com.example.viegymapp.dto.response.ProgramStatsResponse;


import java.util.List;
import java.util.UUID;

public interface WorkoutProgramService {
    List<WorkoutProgramResponse> getAllPrograms();
    
    List<WorkoutProgramResponse> getAdminPrograms();
    
    List<WorkoutProgramResponse> getPopularPrograms(int limit);
    
    List<WorkoutProgramResponse> getSavedPrograms(UUID userId);

    WorkoutProgramResponse getProgramById(UUID id);

    WorkoutProgramResponse createProgram(WorkoutProgramRequest request);

    WorkoutProgramResponse createUserProgram(WorkoutProgramRequest request);

    WorkoutProgramResponse updateProgram(UUID id, WorkoutProgramRequest request);

    void deleteProgram(UUID id);

    List<ProgramExerciseResponse> getExercisesInProgram(UUID programId);

    ProgramExerciseResponse addExerciseToProgram(UUID programId, ProgramExerciseRequest request);

    ProgramExerciseResponse updateProgramExercise(UUID programExerciseId, ProgramExerciseRequest request);

    void deleteProgramExercise(UUID programExerciseId);
    
    // Rating methods
    ProgramRatingResponse rateProgram(UUID programId, UUID userId, ProgramRatingRequest request);
    
    List<ProgramRatingResponse> getProgramRatings(UUID programId);
    
    ProgramStatsResponse getProgramStats(UUID programId, UUID userId);
    
    // Save/Unsave methods
    void saveProgram(UUID programId, UUID userId);
    
    void unsaveProgram(UUID programId, UUID userId);
    
    boolean isProgramSaved(UUID programId, UUID userId);
}
