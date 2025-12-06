package com.example.viegymapp.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResponse {
    private UUID id;
    private String name;
    private String description;
    private String muscleGroup;
    private String difficulty;
    private String exerciseType;
    private Set<String> tags;
    private JsonNode metadata;
    private List<ExerciseMediaResponse> mediaList;
    private UUID createdById;
}
