package com.example.viegymapp.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthLogResponse {
    private UUID id;
    private Instant recordedAt;
    private Double weightKg;
    private Double bodyFatPercent;
    private Double muscleMassKg;
    private Double waistCm;
    private Integer heartRate;
    private String note;
}
