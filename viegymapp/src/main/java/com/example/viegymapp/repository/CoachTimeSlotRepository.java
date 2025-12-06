package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CoachTimeSlot;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoachTimeSlotRepository extends JpaRepository<CoachTimeSlot, UUID> {
    
    @Query("SELECT t FROM CoachTimeSlot t WHERE t.coach = :coach AND t.deleted = false ORDER BY t.startTime DESC")
    List<CoachTimeSlot> findByCoach(@Param("coach") User coach);
    
    @Query("SELECT t FROM CoachTimeSlot t WHERE t.coach.id = :coachId AND t.status = 'AVAILABLE' AND t.startTime >= :now AND t.deleted = false ORDER BY t.startTime")
    List<CoachTimeSlot> findAvailableSlotsByCoach(@Param("coachId") UUID coachId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM CoachTimeSlot t WHERE t.coach.id = :coachId AND t.startTime >= :start AND t.endTime <= :end AND t.deleted = false ORDER BY t.startTime")
    List<CoachTimeSlot> findSlotsByCoachAndDateRange(@Param("coachId") UUID coachId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM CoachTimeSlot t WHERE t.status = 'AVAILABLE' AND t.startTime >= :now AND t.deleted = false ORDER BY t.startTime")
    List<CoachTimeSlot> findAllAvailableSlots(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) > 0 FROM CoachTimeSlot t WHERE t.coach.id = :coachId AND t.deleted = false " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    boolean existsOverlappingSlot(@Param("coachId") UUID coachId, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(t) > 0 FROM CoachTimeSlot t WHERE t.coach.id = :coachId AND t.deleted = false " +
           "AND t.id != :slotId AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    boolean existsOverlappingSlotExcludingId(@Param("coachId") UUID coachId, 
                                              @Param("slotId") UUID slotId,
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
}
