package com.example.viegymapp.service;

import com.example.viegymapp.dto.DashboardStatsDto;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final CommunityPostRepository postRepository;
    private final NutritionLogRepository nutritionLogRepository;
    private final HealthLogRepository healthLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto.UserStatsDto userStats = getUserStats();
        DashboardStatsDto.ContentStatsDto contentStats = getContentStats();
        DashboardStatsDto.NutritionStatsDto nutritionStats = getNutritionStats();
        DashboardStatsDto.HealthStatsDto healthStats = getHealthStats();
        DashboardStatsDto.EngagementStatsDto engagementStats = getEngagementStats();

        return DashboardStatsDto.builder()
                .userStats(userStats)
                .contentStats(contentStats)
                .nutritionStats(nutritionStats)
                .healthStats(healthStats)
                .engagementStats(engagementStats)
                .build();
    }

    private DashboardStatsDto.UserStatsDto getUserStats() {
        long totalUsers = userRepository.count();
        long totalCoaches = userRepository.countByUserRolesRoleName(PredefinedRole.ROLE_COACH);

        Instant since24h = Instant.now().minusSeconds(24 * 3600);
        long activeLast24h = userRepository.countByLastLoginAfter(since24h);

        // Tính tăng trưởng theo tuần thực tế (4 tuần gần đây)
        List<DashboardStatsDto.WeeklyUserGrowth> weeklyGrowth = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        
        for (int i = 3; i >= 0; i--) {
            OffsetDateTime weekStart = now.minusWeeks(i)
                    .with(weekFields.dayOfWeek(), 1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            OffsetDateTime weekEnd = weekStart.plusWeeks(1);
            
            long newUsers = userRepository.countByCreatedAtBetween(weekStart, weekEnd);
            
            int year = weekStart.getYear();
            int weekNumber = weekStart.get(weekFields.weekOfWeekBasedYear());
            String weekLabel = String.format("%d-W%02d", year, weekNumber);
            
            weeklyGrowth.add(DashboardStatsDto.WeeklyUserGrowth.builder()
                    .weekLabel(weekLabel)
                    .newUsers(newUsers)
                    .build());
        }

        return DashboardStatsDto.UserStatsDto.builder()
                .totalUsers(totalUsers)
                .totalCoaches(totalCoaches)
                .activeLast24h(activeLast24h)
                .newUsersByWeek(weeklyGrowth)
                .build();
    }

    private DashboardStatsDto.ContentStatsDto getContentStats() {
        long totalExercises = exerciseRepository.count();
        long totalPosts = postRepository.count();
        long pendingReports = postRepository.countByStatus("pending");
        return DashboardStatsDto.ContentStatsDto.builder()
                .totalExercises(totalExercises)
                .totalPosts(totalPosts)
                .pendingReports(pendingReports)
                .build();
    }

    private DashboardStatsDto.NutritionStatsDto getNutritionStats() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        
        // FIX: Đếm số users duy nhất đã log, không phải số records
        long usersLogged7d = nutritionLogRepository.countDistinctUsersByLogDateAfter(fromDate);
        long totalUsers = userRepository.count();
        double percentLogged7d = totalUsers == 0 ? 0 : (double) usersLogged7d / totalUsers * 100;

        Double avgCalories = nutritionLogRepository.avgCaloriesSince(fromDate);
        if (avgCalories == null) avgCalories = 0.0;

        Double avgDaysLogged = nutritionLogRepository.avgDaysLoggedPerUser(fromDate);
        if (avgDaysLogged == null) avgDaysLogged = 0.0;

        return DashboardStatsDto.NutritionStatsDto.builder()
                .percentLogged7d(percentLogged7d)
                .avgCalories(avgCalories)
                .avgDaysLoggedPerUser(avgDaysLogged)
                .build();
    }

    private DashboardStatsDto.HealthStatsDto getHealthStats() {
        Instant now = Instant.now();
        Instant startDate30d = now.minusSeconds(30 * 24 * 3600L);
        
        // FIX: Đếm số users duy nhất đã update, không phải số records
        long usersUpdated30d = healthLogRepository.countDistinctUsersByRecordedAtAfter(startDate30d);
        long totalUsers = userRepository.count();
        double percentUpdated30d = totalUsers == 0 ? 0 : (double) usersUpdated30d / totalUsers * 100;

        // Cải thiện cách tính weight change: So sánh 7 ngày gần nhất với 7 ngày trước đó
        Instant recent7dStart = now.minusSeconds(7 * 24 * 3600L);
        Instant previous7dStart = now.minusSeconds(14 * 24 * 3600L);
        Instant previous7dEnd = recent7dStart;
        
        Double avgWeightRecent = healthLogRepository.avgWeightBetween(recent7dStart, now);
        Double avgWeightPrevious = healthLogRepository.avgWeightBetween(previous7dStart, previous7dEnd);
        
        double avgWeightChange = 0.0;
        if (avgWeightRecent != null && avgWeightPrevious != null && avgWeightPrevious > 0) {
            avgWeightChange = avgWeightRecent - avgWeightPrevious;
        }

        // Tính phân bố users theo xu hướng weight (30 ngày qua)
        int usersDecreased = 0;
        int usersIncreased = 0;
        int usersMaintained = 0;
        
        // Simplified: Này cần query phức tạp hơn, tạm thời để 0
        // Có thể implement sau bằng cách compare first vs last weight của mỗi user
        
        // Tính BMI trung bình (cần có height data)
        Double avgWeight = healthLogRepository.avgWeightOverall();
        double avgBmi = 0.0; // Tạm thời 0, cần height để tính BMI

        return DashboardStatsDto.HealthStatsDto.builder()
                .percentUpdated30d(percentUpdated30d)
                .avgWeightChangeKg(avgWeightChange)
                .usersWeightDecreased(usersDecreased)
                .usersWeightIncreased(usersIncreased)
                .usersWeightMaintained(usersMaintained)
                .avgBmi(avgBmi)
                .build();
    }

    private DashboardStatsDto.EngagementStatsDto getEngagementStats() {
        Instant now = Instant.now();
        Instant last7d = now.minusSeconds(7 * 24 * 3600L);
        Instant last30d = now.minusSeconds(30 * 24 * 3600L);

        // Tính workout completion rate (sessions có duration > 0)
        long totalSessions7d = workoutSessionRepository.countBySessionDateAfter(last7d);
        long completedSessions7d = workoutSessionRepository.countCompletedSessionsAfter(last7d);
        double workoutCompletionRate = totalSessions7d == 0 ? 0 : (double) completedSessions7d / totalSessions7d * 100;

        // Số users active (có bất kỳ activity nào: login, workout, nutrition log, health log)
        long activeUsers7d = workoutSessionRepository.countDistinctUsersBySessionDateAfter(last7d);
        long activeUsers30d = workoutSessionRepository.countDistinctUsersBySessionDateAfter(last30d);

        // Retention rate: % users hoạt động trong 7 ngày qua so với 30 ngày qua
        double retentionRate7d = activeUsers30d == 0 ? 0 : (double) activeUsers7d / activeUsers30d * 100;

        return DashboardStatsDto.EngagementStatsDto.builder()
                .workoutCompletionRate(workoutCompletionRate)
                .retentionRate7d(retentionRate7d)
                .activeUsersLast7d(activeUsers7d)
                .activeUsersLast30d(activeUsers30d)
                .build();
    }
}
