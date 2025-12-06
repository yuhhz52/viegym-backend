package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignProgramRequest {
    @NotNull(message = "Client ID is required")
    private UUID clientId;
    
    @NotNull(message = "Program ID is required")
    private UUID programId;
    
    private String notes;
}
