package com.example.viegymapp.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
