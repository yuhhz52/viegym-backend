package com.example.viegymapp.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionExerciseLogResponse {
    private UUID id;
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
    private Boolean completed;
    
    // Computed fields
    private Double volume;
    private String displayValue;
}
