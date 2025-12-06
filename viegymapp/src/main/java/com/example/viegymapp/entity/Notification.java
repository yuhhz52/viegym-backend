package com.example.viegymapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String title;

    @Column
    private String link; // URL to navigate when clicked

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean emailSent = false;

    @Column(nullable = false)
    private Boolean pushSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    // Additional metadata (JSON)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    public enum NotificationType {
        ACHIEVEMENT,        // Thành tựu (milestone)
        WORKOUT,           // Hoàn thành workout
        STREAK,            // Chuỗi ngày tập
        SYSTEM,            // Thông báo hệ thống
        REMINDER,          // Nhắc nhở
        SOCIAL,            // Tương tác xã hội
        COACH_MESSAGE,     // Tin nhắn từ coach
        BOOKING_CONFIRMED, // Xác nhận booking
        BOOKING_CANCELLED, // Hủy booking
        PROGRAM_UPDATE     // Cập nhật chương trình
    }
}
