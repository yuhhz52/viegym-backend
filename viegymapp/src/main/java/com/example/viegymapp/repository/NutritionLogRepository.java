package com.example.viegymapp.repository;

import com.example.viegymapp.entity.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, UUID> {

    List<NutritionLog> findByUserId(UUID userId);

    List<NutritionLog> findByUserIdAndLogDate(UUID userId, LocalDate logDate);

    long countByLogDateAfter(LocalDate date);

    @Query("SELECT AVG(n.calories) FROM NutritionLog n WHERE n.logDate >= :fromDate")
    Double avgCaloriesSince(@Param("fromDate") LocalDate fromDate);

    @Query("SELECT COUNT(DISTINCT n.user.id) FROM NutritionLog n WHERE n.logDate >= :fromDate")
    long countDistinctUsersByLogDateAfter(@Param("fromDate") LocalDate fromDate);

    @Query("SELECT AVG(dayCount) FROM (SELECT COUNT(DISTINCT n.logDate) as dayCount FROM NutritionLog n WHERE n.logDate >= :fromDate GROUP BY n.user.id)")
    Double avgDaysLoggedPerUser(@Param("fromDate") LocalDate fromDate);
}
