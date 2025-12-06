package com.example.viegymapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.viegymapp.entity.Enum.UserStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private Integer heightCm;
    private Double weightKg;
    private Double bodyFatPercent;
    private String experienceLevel;
    private String goal;
    private String avatarUrl;
    private Boolean isActive;
    private Set<String> roles;
    private Set<RoleAssignmentInfo> roleAssignments;
    private UserStatus status;

    // Stats
    private Integer streakDays;
    private LocalDate lastStreakUpdate;
    private Integer totalWorkouts;
    private Double totalVolume;

    // Daily Goals
    private Integer dailyWaterGoal;
    private Integer dailyCalorieGoal;
    private Integer dailyWorkoutMins;

    // Settings
    private Boolean darkMode;
    private Boolean notifications;
    private String language;

}
