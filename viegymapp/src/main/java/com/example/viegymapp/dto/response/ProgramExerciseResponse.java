package com.example.viegymapp.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramExerciseResponse {
    private UUID id;
    private Integer dayOfProgram;
    private Integer orderNo;
    private Integer sets;
    private String reps;
    private String weightScheme;
    private Integer restSeconds;
    private String notes;
    private ExerciseShortResponse exercise;

}
