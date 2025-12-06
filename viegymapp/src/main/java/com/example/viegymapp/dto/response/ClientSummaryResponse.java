package com.example.viegymapp.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSummaryResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;
}
