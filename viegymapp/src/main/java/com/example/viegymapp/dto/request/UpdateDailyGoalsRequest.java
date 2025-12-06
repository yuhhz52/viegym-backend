package com.example.viegymapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDailyGoalsRequest {
    private Integer dailyWaterGoal;
    private Integer dailyCalorieGoal;
    private Integer dailyWorkoutMins;
}