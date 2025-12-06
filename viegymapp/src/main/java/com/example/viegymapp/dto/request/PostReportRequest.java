package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportRequest {
    
    @NotBlank(message = "Reason is required")
    private String reason; // spam, inappropriate, harassment, false_info, other
    
    private String description; // Optional detailed description
}
