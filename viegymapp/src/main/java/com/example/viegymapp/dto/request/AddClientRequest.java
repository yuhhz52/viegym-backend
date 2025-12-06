package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddClientRequest {
    @NotNull(message = "Client ID is required")
    private UUID clientId;
    
    private String notes;
}
