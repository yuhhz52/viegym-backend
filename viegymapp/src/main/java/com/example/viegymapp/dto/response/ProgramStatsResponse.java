package com.example.viegymapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramStatsResponse {
    private UUID programId;
    private Double averageRating;
    private Long totalRatings;
    private Long totalSaves;
    private Boolean isSaved;
    private Integer userRating;
}
