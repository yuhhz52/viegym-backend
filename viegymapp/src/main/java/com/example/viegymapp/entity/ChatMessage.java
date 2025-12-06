package com.example.viegymapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(nullable = false)
    LocalDateTime sentAt;

    @Column(nullable = false)
    Boolean isRead = false;

    @Column
    LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageType type = MessageType.TEXT;

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        FILE
    }
}
