package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.entity.Notification;
import com.example.viegymapp.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Async notification service using RabbitMQ
 * Publishes notification events to queue for non-blocking processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationService {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish notification event to RabbitMQ asynchronously
     */
    public void publishNotificationEvent(
            UUID userId,
            Notification.NotificationType type,
            String title,
            String message,
            String link
    ) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .message(message)
                    .link(link)
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            
            log.debug("Published notification event for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
            // Don't throw - notification is not critical for main flow
        }
    }
    
    /**
     * Publish booking notification
     */
    public void publishBookingNotification(
            UUID userId,
            String coachName,
            String timeSlot,
            Notification.NotificationType type
    ) {
        String title = type == Notification.NotificationType.BOOKING_CONFIRMED 
                ? "Đặt lịch thành công" 
                : "Lịch đã bị hủy";
        String message = type == Notification.NotificationType.BOOKING_CONFIRMED
                ? String.format("Đã đặt lịch với %s vào %s", coachName, timeSlot)
                : String.format("Đã hủy lịch với %s (%s)", coachName, timeSlot);
        
        publishNotificationEvent(userId, type, title, message, "/booking");
    }
    
    /**
     * Publish coach message notification
     */
    public void publishCoachMessageNotification(
            UUID userId,
            String coachName,
            String messagePreview
    ) {
        publishNotificationEvent(
                userId,
                Notification.NotificationType.COACH_MESSAGE,
                "Tin nhắn mới từ Coach",
                String.format("%s: %s", coachName, messagePreview),
                "/chat"
        );
    }
    
    /**
     * Publish program update notification
     */
    public void publishProgramNotification(
            UUID userId,
            String creatorName,
            String programTitle
    ) {
        publishNotificationEvent(
                userId,
                Notification.NotificationType.PROGRAM_UPDATE,
                "Chương trình mới",
                String.format("Bạn đã lưu chương trình: %s", programTitle),
                "/training"
        );
    }
    
    /**
     * Publish achievement notification
     */
    public void publishAchievementNotification(
            UUID userId,
            String achievementMessage
    ) {
        publishNotificationEvent(
                userId,
                Notification.NotificationType.ACHIEVEMENT,
                "Thành tựu mới",
                achievementMessage,
                "/progress"
        );
    }
}
