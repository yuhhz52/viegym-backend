package com.example.viegymapp.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLogResponse {
    private UUID id;
    private UUID userId;
    private String email;
    private LocalDate logDate;
    private String mealType;
    private Integer calories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatsG;
    private String notes;
}
