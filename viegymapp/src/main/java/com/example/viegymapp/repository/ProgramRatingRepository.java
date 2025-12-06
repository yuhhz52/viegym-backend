package com.example.viegymapp.repository;

import com.example.viegymapp.entity.ProgramRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRatingRepository extends JpaRepository<ProgramRating, UUID> {
    
    Optional<ProgramRating> findByProgramIdAndUserId(UUID programId, UUID userId);
    
    List<ProgramRating> findByProgramId(UUID programId);
    
    @Query("SELECT AVG(r.rating) FROM ProgramRating r WHERE r.program.id = :programId")
    Double getAverageRating(@Param("programId") UUID programId);
    
    @Query("SELECT COUNT(r) FROM ProgramRating r WHERE r.program.id = :programId")
    Long getRatingCount(@Param("programId") UUID programId);
    
    void deleteByProgramId(UUID programId);
    
    // Get programs with most ratings for Popular Programs
    @Query("SELECT r.program.id, COUNT(r) as ratingCount FROM ProgramRating r " +
           "GROUP BY r.program.id ORDER BY ratingCount DESC")
    List<Object[]> findProgramsByRatingCount();
}
