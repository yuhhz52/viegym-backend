package com.example.viegymapp.repository;

import com.example.viegymapp.entity.Enum.DifficultyLevel;
import com.example.viegymapp.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    // Tìm theo tag (join table tags)
    Page<Exercise> findDistinctByTags_Name(String tag, Pageable pageable);

    // Tìm theo difficulty
    Page<Exercise> findByDifficulty(DifficultyLevel difficulty, Pageable pageable);

    // Tìm theo muscle group
    Page<Exercise> findByMuscleGroup(String muscleGroup, Pageable pageable);

    // Tìm theo tag + difficulty
    Page<Exercise> findDistinctByTags_NameAndDifficulty(String tag, DifficultyLevel difficulty, Pageable pageable);

    // Tìm theo tag + muscle group
    Page<Exercise> findDistinctByTags_NameAndMuscleGroup(String tag, String muscleGroup, Pageable pageable);

    // Tìm theo difficulty + muscle group
    Page<Exercise> findByDifficultyAndMuscleGroup(DifficultyLevel difficulty, String muscleGroup, Pageable pageable);

    // Tìm theo tag + difficulty + muscle group
    Page<Exercise> findDistinctByTags_NameAndDifficultyAndMuscleGroup(
            String tag,
            DifficultyLevel difficulty,
            String muscleGroup,
            Pageable pageable
    );
}
