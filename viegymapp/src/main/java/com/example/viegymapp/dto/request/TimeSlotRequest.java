package com.example.viegymapp.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeSlotRequest {
    LocalDateTime startTime;
    LocalDateTime endTime;
    String notes;
    String location;
    BigDecimal price;
    Integer capacity; // Optional: default 1 for 1-1 PT, >1 for group classes
}
