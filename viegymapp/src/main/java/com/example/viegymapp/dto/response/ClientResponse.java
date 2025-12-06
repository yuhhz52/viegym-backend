package com.example.viegymapp.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private Integer heightCm;
    private Double weightKg;
    private String goal;
    private String experienceLevel;
    private String avatarUrl;
    private Integer totalWorkouts;
    private Double totalVolume;
    private Integer streakDays;
    private Instant joinedDate;
    private String status;
    private String notes;
}
