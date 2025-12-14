package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CoachTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachTransactionRepository extends JpaRepository<CoachTransaction, UUID> {
    
    List<CoachTransaction> findByCoachIdOrderByCreatedAtDesc(UUID coachId);
    
    /**
     * Get the most recent transaction for a coach (used for accurate balance calculation)
     */
    @Query("""
        SELECT ct FROM CoachTransaction ct
        WHERE ct.coach.id = :coachId
        ORDER BY ct.createdAt DESC, ct.processedAt DESC
        LIMIT 1
    """)
    Optional<CoachTransaction> findTopByCoachIdOrderByCreatedAtDescProcessedAtDesc(@Param("coachId") UUID coachId);
    
    List<CoachTransaction> findByCoachIdAndTypeOrderByCreatedAtDesc(
        UUID coachId, 
        CoachTransaction.TransactionType type
    );
    
    List<CoachTransaction> findByPaymentId(UUID paymentId);
}
