package com.example.viegymapp.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseMediaRequest {

    @NotBlank(message = "Media type is required")
    private String mediaType; // image, video

    @NotBlank(message = "URL is required")
    private String url;

    private String caption;
    private Integer orderNo;
}
