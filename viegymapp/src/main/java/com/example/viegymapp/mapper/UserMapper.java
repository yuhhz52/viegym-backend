package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.UserCreationRequest;
import com.example.viegymapp.dto.request.UserUpdateRequest;
import com.example.viegymapp.dto.request.UpdateProfileRequest;
import com.example.viegymapp.dto.request.UpdateSettingsRequest;
import com.example.viegymapp.dto.request.UpdateDailyGoalsRequest;
import com.example.viegymapp.dto.response.UserResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.UserRole;
import org.mapstruct.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    // ============ CREATE ============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", expression = "java(new java.util.HashSet<>())")
    User toUser(UserCreationRequest request);

    // ============ TO RESPONSE ============
    @Mapping(target = "roles", expression = "java(mapRoles(user.getUserRoles()))")
    @Mapping(target = "roleAssignments", expression = "java(mapRoleAssignments(user.getUserRoles()))")
    @Mapping(target = "status", source = "status")
    UserResponse toUserResponse(User user);

    // ============ UPDATE USER (Admin) ============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    // ============ UPDATE PROFILE ============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    void updateProfile(@MappingTarget User user, UpdateProfileRequest request);

    // ============ UPDATE SETTINGS ============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateSettings(@MappingTarget User user, UpdateSettingsRequest request);

    // ============ UPDATE DAILY GOALS ============
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateDailyGoals(@MappingTarget User user, UpdateDailyGoalsRequest request);

    // ============ HELPER: Map Roles ============
    default Set<String> mapRoles(Set<UserRole> userRoles) {
        if (userRoles == null) return Collections.emptySet();
        return userRoles.stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet());
    }

    default Set<com.example.viegymapp.dto.response.RoleAssignmentInfo> mapRoleAssignments(Set<UserRole> userRoles) {
        if (userRoles == null) return Collections.emptySet();
        return userRoles.stream()
                .map(ur -> com.example.viegymapp.dto.response.RoleAssignmentInfo.builder()
                        .roleName(ur.getRole().getName().name())
                        .assignedByName(ur.getAssignedBy() != null ? ur.getAssignedBy().getFullName() : null)
                        .assignedByEmail(ur.getAssignedBy() != null ? ur.getAssignedBy().getEmail() : null)
                        .assignedAt(ur.getCreatedAt())
                        .build())
                .collect(Collectors.toSet());
    }
}