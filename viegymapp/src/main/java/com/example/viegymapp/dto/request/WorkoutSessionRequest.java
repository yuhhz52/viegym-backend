package com.example.viegymapp.dto.request;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSessionRequest {
    private UUID programId;
    private Instant sessionDate;
    private Integer durationMinutes;
    private String notes;
}
