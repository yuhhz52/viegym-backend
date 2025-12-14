package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.BookingSession;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    UUID id;
    UUID coachId;
    String coachName;
    String coachAvatarUrl;
    UUID clientId;
    String clientName;
    TimeSlotResponse timeSlot;
    LocalDateTime bookingTime;
    BookingSession.BookingStatus status;
    String clientNotes;
    String coachNotes;
    LocalDateTime expiredAt; // Booking expires if not paid within 10 minutes (for PENDING bookings)
}
