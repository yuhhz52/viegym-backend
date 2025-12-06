package com.example.viegymapp.dto.request;

import com.example.viegymapp.entity.Notification;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private Notification.NotificationType type;
    private String message;
    private String title;
    private String link;
    private String metadata;
}
