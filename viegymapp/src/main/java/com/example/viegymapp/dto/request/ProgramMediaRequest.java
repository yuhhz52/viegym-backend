package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramMediaRequest {
    @NotBlank(message = "Media type is required")
    private String mediaType;

    @NotBlank(message = "URL is required")
    private String url;

    private String caption;
    private Integer orderNo;
}
