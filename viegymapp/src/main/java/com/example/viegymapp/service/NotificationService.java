package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.NotificationPreferenceRequest;
import com.example.viegymapp.dto.request.NotificationRequest;
import com.example.viegymapp.dto.response.NotificationPreferenceResponse;
import com.example.viegymapp.dto.response.NotificationResponse;
import com.example.viegymapp.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    // Notification CRUD
    NotificationResponse createNotification(UUID userId, NotificationRequest request);
    Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable);
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    long getUnreadCount(UUID userId);
    NotificationResponse markAsRead(UUID notificationId, UUID userId);
    void markAllAsRead(UUID userId);
    void deleteNotification(UUID notificationId, UUID userId);
    void deleteAllNotifications(UUID userId);
    
    // Auto-generate notifications based on events
    void generateWorkoutCompletionNotification(UUID userId, int durationMinutes, double volume);
    void generateAchievementNotification(UUID userId, String achievementType, Object... params);
    void generateStreakNotification(UUID userId, int streakDays);
    void generateReminderNotification(UUID userId, String message);
    void generateBookingNotification(UUID userId, String coachName, String timeSlot, Notification.NotificationType type);
    
    // Notification preferences
    NotificationPreferenceResponse getPreferences(UUID userId);
    NotificationPreferenceResponse updatePreferences(UUID userId, NotificationPreferenceRequest request);
    
    // Send via channels
    void sendEmailNotification(UUID userId, Notification notification);
    void sendPushNotification(UUID userId, Notification notification);
}
