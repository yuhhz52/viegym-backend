package com.example.viegymapp.repository;

import com.example.viegymapp.entity.ProgramExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProgramExerciseRepository extends JpaRepository<ProgramExercise, UUID> {
    List<ProgramExercise> findByProgramId(UUID programId);
    void deleteByProgramId(UUID programId);
}
