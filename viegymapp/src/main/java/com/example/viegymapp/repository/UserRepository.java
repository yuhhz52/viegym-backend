package com.example.viegymapp.repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.entity.Enum.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByIdAndStatusNot(UUID id, UserStatus status);

    @Query("SELECT COUNT(u) FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    int countByUserRolesRoleName(@Param("roleName") PredefinedRole roleName);


    long countByLastLoginAfter(Instant timestamp);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    // Update avatar
    @Modifying
    @Query("UPDATE User u SET u.avatarUrl = :avatarUrl WHERE u.id = :userId")
    void updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    // Update settings
    @Modifying
    @Query("UPDATE User u SET u.darkMode = :darkMode, u.notifications = :notifications, u.language = :language WHERE u.id = :userId")
    void updateSettings(@Param("userId") Long userId,
                        @Param("darkMode") Boolean darkMode,
                        @Param("notifications") Boolean notifications,
                        @Param("language") String language);

    // Update daily goals
    @Modifying
    @Query("UPDATE User u SET u.dailyWaterGoal = :water, u.dailyCalorieGoal = :calories, u.dailyWorkoutMins = :workout WHERE u.id = :userId")
    void updateDailyGoals(@Param("userId") Long userId,
                          @Param("water") Integer dailyWaterGoal,
                          @Param("calories") Integer dailyCalorieGoal,
                          @Param("workout") Integer dailyWorkoutMins);

    // Update stats after workout
    @Modifying
    @Query("UPDATE User u SET u.totalWorkouts = u.totalWorkouts + 1, u.totalVolume = u.totalVolume + :volume WHERE u.id = :userId")
    void incrementWorkoutStats(@Param("userId") Long userId, @Param("volume") Double volume);

    // Update streak
    @Modifying
    @Query("UPDATE User u SET u.streakDays = :streak WHERE u.id = :userId")
    void updateStreak(@Param("userId") Long userId, @Param("streak") Integer streakDays);
}
