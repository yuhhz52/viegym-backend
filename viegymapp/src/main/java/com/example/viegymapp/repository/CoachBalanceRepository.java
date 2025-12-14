package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CoachBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachBalanceRepository extends JpaRepository<CoachBalance, UUID> {
    
    Optional<CoachBalance> findByCoachId(UUID coachId);
    
    boolean existsByCoachId(UUID coachId);
}
