package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private Notification.NotificationType type;
    private String message;
    private String title;
    private String link;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String metadata;
}
