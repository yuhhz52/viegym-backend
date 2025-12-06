package com.example.viegymapp.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramExerciseRequest {
    private String exerciseId;   // UUID exercise
    private Integer dayOfProgram;
    private Integer orderNo;
    private Integer sets;
    private String reps;
    private String weightScheme;
    private Integer restSeconds;
    private String notes;
}
