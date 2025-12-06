package com.example.viegymapp.service.Impl;

import com.example.viegymapp.entity.User;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.repository.WorkoutSessionRepository;
import com.example.viegymapp.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakServiceImpl implements StreakService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void calculateAndUpdateStreak(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate lastUpdate = user.getLastStreakUpdate();

        // Chỉ tính streak nếu chưa update hôm nay
        if (lastUpdate != null && lastUpdate.equals(today)) {
            log.info("User {} streak already updated today, skipping", userId);
            return;
        }

        boolean hasTodayWorkout = hasWorkoutToday(userId);
        boolean hasYesterdayWorkout = hasWorkoutYesterday(userId);

        if (hasTodayWorkout) {
            // Kiểm tra xem có phải ngày mới so với lần update trước không
            if (lastUpdate == null) {
                // Lần đầu tiên tính streak
                user.setStreakDays(1);
                log.info("User {} first streak day, streak set to 1", userId);
            } else if (lastUpdate.plusDays(1).equals(today)) {
                // Ngày hôm nay là ngày tiếp theo sau lần update trước → tăng streak
                user.setStreakDays(user.getStreakDays() + 1);
                log.info("User {} has workout today (new day), streak increased to {}", userId, user.getStreakDays());
            } else if (lastUpdate.isBefore(today.minusDays(1))) {
                // Đã bỏ lỡ ít nhất 1 ngày → reset streak về 1
                user.setStreakDays(1);
                log.info("User {} missed days, streak reset to 1", userId);
            }
            // Cập nhật lastStreakUpdate
            user.setLastStreakUpdate(today);
        } else if (lastUpdate != null && !hasYesterdayWorkout && lastUpdate.isBefore(today.minusDays(1))) {
            // Nếu cả hôm nay và hôm qua không có workout và đã bỏ lỡ nhiều ngày: reset streak
            user.setStreakDays(0);
            user.setLastStreakUpdate(null);
            log.info("User {} has no workout today and yesterday, streak reset to 0", userId);
        }
        // Nếu hôm nay không workout nhưng hôm qua có: giữ nguyên streak

        userRepository.save(user);
    }

    @Override
    public boolean hasWorkoutToday(UUID userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        long count = workoutSessionRepository.countByUserIdAndSessionDateBetween(
                userId,
                today.atStartOfDay().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),
                today.plusDays(1).atStartOfDay().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        );
        return count > 0;
    }

    @Override
    public boolean hasWorkoutYesterday(UUID userId) {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(1);
        long count = workoutSessionRepository.countByUserIdAndSessionDateBetween(
                userId,
                yesterday.atStartOfDay().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),
                yesterday.plusDays(1).atStartOfDay().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()
        );
        return count > 0;
    }

    @Override
    @Transactional
    public void resetStreak(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setStreakDays(0);
        user.setLastStreakUpdate(null);
        userRepository.save(user);
        log.info("User {} streak reset to 0", userId);
    }
}
