package com.example.viegymapp.dto.request;

import com.example.viegymapp.validator.DobConstraint;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    private String email;
    private String password;
    private String fullName;
    private String gender;
    @DobConstraint(min = 16, message = "INVALID_DOB")
    private LocalDate birthDate;
    private String phone;
    private Integer heightCm;
    private Double weightKg;
    private Double bodyFatPercent;
    private String experienceLevel;
    private String goal;
    private String avatarUrl;
    Set<String> roles;
}
