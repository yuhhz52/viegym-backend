package com.example.viegymapp.dto.request;

import com.example.viegymapp.entity.Enum.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLogRequest {


    @NotNull
    private LocalDate logDate;

    @NotNull
    private MealType mealType;

    @NotNull
    private Integer calories;

    private Integer proteinG;
    private Integer carbsG;
    private Integer fatsG;

    private String notes;
}
