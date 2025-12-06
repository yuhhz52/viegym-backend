package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.NotificationPreferenceRequest;
import com.example.viegymapp.dto.request.NotificationRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.NotificationPreferenceResponse;
import com.example.viegymapp.dto.response.NotificationResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    /**
     * Get current authenticated user from security context
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    
    /**
     * Get paginated notifications for current user
     */
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = getCurrentUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.getNotifications(userId, pageable))
                .build();
    }
    
    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications() {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getUnreadNotifications(userId))
                .build();
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ApiResponse<Long> getUnreadCount() {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount(userId))
                .build();
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable UUID id
    ) {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.markAsRead(id, userId))
                .build();
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        UUID userId = getCurrentUser().getId();
        notificationService.markAllAsRead(userId);
        return ApiResponse.<Void>builder()
                .message("Đã đánh dấu tất cả thông báo là đã đọc")
                .build();
    }
    
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(
            @PathVariable UUID id
    ) {
        UUID userId = getCurrentUser().getId();
        notificationService.deleteNotification(id, userId);
        return ApiResponse.<Void>builder()
                .message("Xóa thông báo thành công")
                .build();
    }
    
    /**
     * Delete all notifications
     */
    @DeleteMapping
    public ApiResponse<Void> deleteAllNotifications() {
        UUID userId = getCurrentUser().getId();
        notificationService.deleteAllNotifications(userId);
        return ApiResponse.<Void>builder()
                .message("Đã xóa tất cả thông báo")
                .build();
    }
    
    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> getPreferences() {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<NotificationPreferenceResponse>builder()
                .result(notificationService.getPreferences(userId))
                .build();
    }
    
    /**
     * Update notification preferences
     */
    @PutMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> updatePreferences(
            @RequestBody NotificationPreferenceRequest request
    ) {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<NotificationPreferenceResponse>builder()
                .result(notificationService.updatePreferences(userId, request))
                .build();
    }
    
    /**
     * Create notification manually (for testing)
     */
    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(
            @RequestBody NotificationRequest request
    ) {
        UUID userId = getCurrentUser().getId();
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.createNotification(userId, request))
                .message("Tạo thông báo thành công")
                .build();
    }
    
    /**
     * Generate sample notifications (for testing)
     */
    @PostMapping("/test/generate")
    public ApiResponse<Void> generateTestNotifications() {
        UUID userId = getCurrentUser().getId();
        
        // Generate various test notifications
        notificationService.generateAchievementNotification(userId, "FIRST_WORKOUT", "");
        notificationService.generateWorkoutCompletionNotification(userId, 45, 1250.5);
        notificationService.generateStreakNotification(userId, 7);
        notificationService.generateReminderNotification(userId, "Đừng quên tập luyện hôm nay!");
        notificationService.generateBookingNotification(userId, "Coach John", "10:00 AM - 11:00 AM", 
            com.example.viegymapp.entity.Notification.NotificationType.BOOKING_CONFIRMED);
        
        return ApiResponse.<Void>builder()
                .message("Đã tạo 5 thông báo mẫu")
                .build();
    }
}
