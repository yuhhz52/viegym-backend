package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {

    @NotBlank
    @Size(min = 6 , message = "INVALID_EMAIL")
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;

    private String fullName;

    private String phone;

    private Set<String> role;
}
