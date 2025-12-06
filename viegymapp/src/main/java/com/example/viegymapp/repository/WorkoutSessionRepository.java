package com.example.viegymapp.repository;

import com.example.viegymapp.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    List<WorkoutSession> findByUserId(UUID userId);

    // Count workouts by user and date range
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.sessionDate >= :startDate AND ws.sessionDate < :endDate")
    long countByUserIdAndSessionDateBetween(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    // Count workouts for multiple users
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user.id IN :userIds")
    long countByUserIdIn(@Param("userIds") List<UUID> userIds);

    // Count sessions in date range
    long countBySessionDateAfter(Instant dateTime);

    // Count distinct users with sessions after date
    @Query("SELECT COUNT(DISTINCT ws.user.id) FROM WorkoutSession ws WHERE ws.sessionDate >= :dateTime")
    long countDistinctUsersBySessionDateAfter(@Param("dateTime") Instant dateTime);

    // Count completed sessions (with duration > 0) after date
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.sessionDate >= :dateTime AND ws.durationMinutes IS NOT NULL AND ws.durationMinutes > 0")
    long countCompletedSessionsAfter(@Param("dateTime") Instant dateTime);
}

