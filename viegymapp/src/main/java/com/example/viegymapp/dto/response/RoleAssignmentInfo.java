package com.example.viegymapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentInfo {
    private String roleName;
    private String assignedByName;
    private String assignedByEmail;
    private OffsetDateTime assignedAt;
}
