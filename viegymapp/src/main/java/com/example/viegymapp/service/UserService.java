package com.example.viegymapp.service;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.*;
import com.example.viegymapp.dto.response.UserResponse;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.entity.Enum.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface UserService {

    // ============ CURRENT USER ============
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UserUpdateRequest request);
    UserResponse updateProfile(UpdateProfileRequest request);
    UserResponse updateAvatar(MultipartFile file) throws IOException;
    UserResponse updateSettings(UpdateSettingsRequest request);
    UserResponse updateDailyGoals(UpdateDailyGoalsRequest request);

    // ============ WORKOUT STATS ============
    void incrementWorkoutStats(Double volume);
    void updateStreak(Integer streakDays);

    // ============ ADMIN ============
    UserResponse createUser(UserCreationRequest request);
    UserResponse getUserById(UUID id);
    UserResponse updateUserById(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
    PagingResponse<UserResponse> getAllUsers(int page, int size);

    // ============ USER STATUS ============
    UserResponse disableUser(UUID userId);
    UserResponse enableUser(UUID userId);
    UserResponse updateUserStatus(UUID id, UserStatus status);

    // ============ ROLES ============
    UserResponse assignRoleToUser(UUID userId, PredefinedRole roleName);
    UserResponse removeRoleFromUser(UUID userId, PredefinedRole roleName);
}