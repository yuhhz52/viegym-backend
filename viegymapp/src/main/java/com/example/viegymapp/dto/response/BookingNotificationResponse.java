package com.example.viegymapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingNotificationResponse {
    
    UUID bookingId;
    UUID coachId;
    String clientName;
    String clientEmail;
    LocalDateTime bookingTime;
    String timeSlotInfo;
    String message;
    String type; // "NEW_BOOKING", "BOOKING_CANCELLED", etc.
    LocalDateTime timestamp;
}