package com.example.viegymapp.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequest {
    // Email preferences
    private Boolean emailEnabled;
    private Boolean emailAchievements;
    private Boolean emailWorkouts;
    private Boolean emailReminders;
    private Boolean emailSocial;
    private Boolean emailCoach;
    private Boolean emailBooking;

    // Push preferences
    private Boolean pushEnabled;
    private Boolean pushAchievements;
    private Boolean pushWorkouts;
    private Boolean pushReminders;
    private Boolean pushSocial;
    private Boolean pushCoach;
    private Boolean pushBooking;

    // Reminder settings
    private String reminderTime;
    private Boolean dailyReminder;
}
