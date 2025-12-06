package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.ProgramMedia;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutProgramResponse {
    private UUID id;
    private String title;
    private String description;
    private String goal;
    private Integer durationWeeks;
    private String visibility;
    private List<ProgramMediaResponse> mediaList;
    private List<ProgramExerciseResponse> exercises;
    private UUID createdBy;
    private String createdByName;
}
