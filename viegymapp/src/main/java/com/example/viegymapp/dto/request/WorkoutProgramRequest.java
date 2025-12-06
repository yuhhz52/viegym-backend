package com.example.viegymapp.dto.request;

import lombok.*;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutProgramRequest {
    private String title;
    private String description;
    private String goal;
    private Integer durationWeeks;
    private String visibility; // public/private
    private Collection<ProgramMediaRequest> mediaList;

}
