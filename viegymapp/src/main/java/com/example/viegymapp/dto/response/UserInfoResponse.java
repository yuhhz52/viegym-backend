package com.example.viegymapp.dto.response;

import lombok.*;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserInfoResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private String refreshToken;
    private Set<String> roles;
}
