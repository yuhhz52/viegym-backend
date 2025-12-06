package com.example.viegymapp.listener;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.request.NotificationRequest;
import com.example.viegymapp.event.NotificationEvent;
import com.example.viegymapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("Processing notification event for user: {}", event.getUserId());
            
            NotificationRequest request = NotificationRequest.builder()
                    .type(event.getType())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .link(event.getLink())
                    .metadata(event.getMetadata())
                    .build();
            
            notificationService.createNotification(event.getUserId(), request);
            
            log.info("Notification created successfully for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
            // Don't throw exception to avoid message being requeued
        }
    }
}
