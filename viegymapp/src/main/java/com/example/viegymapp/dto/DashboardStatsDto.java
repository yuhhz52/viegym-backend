package com.example.viegymapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private UserStatsDto userStats;
    private ContentStatsDto contentStats;
    private NutritionStatsDto nutritionStats;
    private HealthStatsDto healthStats;
    private EngagementStatsDto engagementStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatsDto {
        private long totalUsers;
        private long totalCoaches;
        private long activeLast24h;
        private List<WeeklyUserGrowth> newUsersByWeek;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyUserGrowth {
        private String weekLabel; // ví dụ "2025-W45"
        private long newUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStatsDto {
        private long totalExercises;
        private long totalPosts;
        private long pendingReports;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionStatsDto {
        private double percentLogged7d;
        private double avgCalories;
        private double avgDaysLoggedPerUser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthStatsDto {
        private double percentUpdated30d;
        private double avgWeightChangeKg;
        private int usersWeightDecreased;
        private int usersWeightIncreased;
        private int usersWeightMaintained;
        private double avgBmi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementStatsDto {
        private double workoutCompletionRate;
        private double retentionRate7d;
        private long activeUsersLast7d;
        private long activeUsersLast30d;
    }
}