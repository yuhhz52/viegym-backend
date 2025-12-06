package com.example.viegymapp.event;

import com.example.viegymapp.entity.Notification;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {
    private UUID userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String link;
    private String metadata;
}
