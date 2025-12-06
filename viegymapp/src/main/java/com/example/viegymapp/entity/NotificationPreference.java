package com.example.viegymapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Email preferences
    @Column(nullable = false)
    private Boolean emailEnabled = true;

    @Column(nullable = false)
    private Boolean emailAchievements = true;

    @Column(nullable = false)
    private Boolean emailWorkouts = false;

    @Column(nullable = false)
    private Boolean emailReminders = true;

    @Column(nullable = false)
    private Boolean emailSocial = true;

    @Column(nullable = false)
    private Boolean emailCoach = true;

    @Column(nullable = false)
    private Boolean emailBooking = true;

    // Push/In-app preferences
    @Column(nullable = false)
    private Boolean pushEnabled = true;

    @Column(nullable = false)
    private Boolean pushAchievements = true;

    @Column(nullable = false)
    private Boolean pushWorkouts = true;

    @Column(nullable = false)
    private Boolean pushReminders = true;

    @Column(nullable = false)
    private Boolean pushSocial = true;

    @Column(nullable = false)
    private Boolean pushCoach = true;

    @Column(nullable = false)
    private Boolean pushBooking = true;

    // Reminder settings
    @Column
    private String reminderTime; // Format: "HH:mm" (e.g., "18:00")

    @Column(nullable = false)
    private Boolean dailyReminder = false;
}
