package com.example.viegymapp.dto.request;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionExerciseLogRequest {
    private UUID sessionId;
    private UUID exerciseId;
    private Integer setNumber;
    private Integer repsDone;
    private Double weightUsed;
    
    // New fields for different exercise types
    private Integer durationSeconds;
    private Double distanceMeters;
    private Double bodyWeight;
    private String setNotes;
}
