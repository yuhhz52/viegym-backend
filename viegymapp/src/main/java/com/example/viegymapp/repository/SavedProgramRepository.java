package com.example.viegymapp.repository;

import com.example.viegymapp.entity.SavedProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedProgramRepository extends JpaRepository<SavedProgram, UUID> {
    
    Optional<SavedProgram> findByProgramIdAndUserId(UUID programId, UUID userId);
    
    List<SavedProgram> findByUserId(UUID userId);
    
    boolean existsByProgramIdAndUserId(UUID programId, UUID userId);
    
    boolean existsByUserIdAndProgramId(UUID userId, UUID programId);
    
    void deleteByProgramIdAndUserId(UUID programId, UUID userId);
    
    void deleteByProgramId(UUID programId);
    
    long countByUserIdIn(List<UUID> userIds);
}
