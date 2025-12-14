package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.CoachTimeSlot;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeSlotResponse {
    UUID id;
    UUID coachId;
    String coachName;
    String coachAvatarUrl;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Boolean isAvailable;
    String notes;
    String location;
    CoachTimeSlot.SlotStatus status;
    BigDecimal price;
    Integer capacity;
    Integer bookedCount;
}
