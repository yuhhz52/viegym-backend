package com.example.viegymapp.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSessionResponse {
    private UUID id;
    private UUID userId;
    private UUID programId;
    private Instant sessionDate;
    private Integer durationMinutes;
    private String notes;
}
