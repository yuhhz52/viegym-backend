package com.example.viegymapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramRatingResponse {
    private UUID id;
    private UUID programId;
    private UUID userId;
    private String userName;
    private Integer rating;
    private String review;
    private Instant createdAt;
}
