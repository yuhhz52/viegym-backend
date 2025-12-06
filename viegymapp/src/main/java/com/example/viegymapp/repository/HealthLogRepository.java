package com.example.viegymapp.repository;

import com.example.viegymapp.entity.HealthLog;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface HealthLogRepository extends JpaRepository<HealthLog, UUID> {
    List<HealthLog> findByUser(User user);

    long countByRecordedAtAfter(Instant dateTime);

    @Query("SELECT AVG(h.weightKg) FROM HealthLog h WHERE h.recordedAt >= :startDate AND h.recordedAt <= :endDate AND h.weightKg IS NOT NULL")
    Double avgWeightBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(DISTINCT h.user.id) FROM HealthLog h WHERE h.recordedAt >= :dateTime")
    long countDistinctUsersByRecordedAtAfter(@Param("dateTime") Instant dateTime);

    @Query("SELECT AVG(h.weightKg) FROM HealthLog h WHERE h.weightKg IS NOT NULL")
    Double avgWeightOverall();

}
