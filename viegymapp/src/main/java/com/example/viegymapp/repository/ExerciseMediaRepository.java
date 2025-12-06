package com.example.viegymapp.repository;

import com.example.viegymapp.entity.ExerciseMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseMediaRepository extends JpaRepository<ExerciseMedia, UUID> {
    List<ExerciseMedia> findByExerciseId(UUID exerciseId);
}
