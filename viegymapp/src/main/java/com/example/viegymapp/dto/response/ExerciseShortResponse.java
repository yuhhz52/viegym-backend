package com.example.viegymapp.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseShortResponse {
    private UUID id;
    private String name;
}
