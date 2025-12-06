package com.example.viegymapp.dto.request;

import com.example.viegymapp.entity.Enum.PredefinedRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {
    private PredefinedRole roleName;

}