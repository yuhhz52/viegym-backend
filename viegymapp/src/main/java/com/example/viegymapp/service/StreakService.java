package com.example.viegymapp.service;

import java.util.UUID;

public interface StreakService {
    /**
     * Tính toán streak cho user dựa trên lịch sử workout
     * - Nếu ngày hôm nay có workout: streak += 1
     * - Nếu ngày hôm nay không có workout nhưng hôm qua có: streak = giữ nguyên
     * - Nếu cả hôm nay và hôm qua không có workout: streak = 0
     */
    void calculateAndUpdateStreak(UUID userId);

    /**
     * Kiểm tra user có workout hôm nay không
     */
    boolean hasWorkoutToday(UUID userId);

    /**
     * Kiểm tra user có workout hôm qua không
     */
    boolean hasWorkoutYesterday(UUID userId);

    /**
     * Reset streak về 0
     */
    void resetStreak(UUID userId);
}
