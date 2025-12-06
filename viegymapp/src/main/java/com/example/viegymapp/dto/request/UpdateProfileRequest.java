package com.example.viegymapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String fullName;
    private String gender;
    private LocalDate birthDate;
    private Double heightCm;
    private Double weightKg;
    private Double bodyFatPercent;
    private String experienceLevel;
    private String goal;
    
    // Daily goals
    private Integer dailyCalorieGoal;
    private Integer dailyWaterGoal;
    private Integer dailyWorkoutMins;
}
