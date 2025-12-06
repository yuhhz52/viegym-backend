package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CoachClient;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoachClientRepository extends JpaRepository<CoachClient, UUID> {
    
    List<CoachClient> findByCoachId(UUID coachId);
    
    List<CoachClient> findByCoachIdAndStatus(UUID coachId, String status);
    
    List<CoachClient> findByClientId(UUID clientId);
    
    Optional<CoachClient> findByCoachIdAndClientId(UUID coachId, UUID clientId);
    
    boolean existsByCoachIdAndClientId(UUID coachId, UUID clientId);
    
    long countByCoachId(UUID coachId);
    
    long countByCoachIdAndStatus(UUID coachId, String status);
    
    @Query("SELECT COUNT(cc) FROM CoachClient cc WHERE cc.coach.id = :coachId AND cc.startedDate >= :startDate")
    long countNewClientsSince(@Param("coachId") UUID coachId, @Param("startDate") Instant startDate);
}
