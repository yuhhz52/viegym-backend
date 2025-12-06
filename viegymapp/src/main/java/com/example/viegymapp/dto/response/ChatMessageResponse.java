package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.ChatMessage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    UUID id;
    UUID senderId;
    String senderName;
    UUID receiverId;
    String receiverName;
    String content;
    LocalDateTime sentAt;
    Boolean isRead;
    LocalDateTime readAt;
    ChatMessage.MessageType type;
}
