package com.example.viegymapp.controller;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.*;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.UserResponse;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ============ AUTH & REGISTRATION ============

    /**
     * Tạo user mới (Register)
     */
    @PostMapping("/register")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
       return ApiResponse.<UserResponse>builder()
               .result(userService.createUser(request))
               .build();
    }

    // ============ CURRENT USER ============

    /**
     * Lấy thông tin user hiện tại
     */
    @GetMapping("/my-info")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> getCurrentUser(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getCurrentUser())
                .build();
    }

    /**
     * Cập nhật thông tin basic của user hiện tại (email, phone, fullName)
     */
    @PatchMapping("/my-info")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateCurrentUser(request))
                .build();
    }

    /**
     * Cập nhật hồ sơ user hiện tại (chiều cao, cân nặng, giới tính, v.v.)
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateProfile(request))
                .build();
    }

    /**
     * Cập nhật avatar user hiện tại
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateAvatar(file))
                .build();
    }

    /**
     * Cập nhật cài đặt user hiện tại (darkMode, notifications, language)
     */
    @PatchMapping("/settings")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateSettings(
            @RequestBody UpdateSettingsRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateSettings(request))
                .build();
    }

    /**
     * Cập nhật mục tiêu hàng ngày user hiện tại (calories, water, workout mins)
     */
    @PatchMapping("/daily-goals")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateDailyGoals(
            @RequestBody UpdateDailyGoalsRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateDailyGoals(request))
                .build();
    }

    // ============ ADMIN - USER MANAGEMENT ============

    /**
     * Lấy user theo ID (Admin)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    /**
     * Lấy danh sách tất cả user (Admin) - Phân trang
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PagingResponse<UserResponse>>getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PagingResponse<UserResponse>>builder()
                .result(userService.getAllUsers(page, size))
                .build();
    }

    /**
     * Cập nhật user theo ID (Admin)
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> updateUserById(@PathVariable UUID id,
                                                    @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserById(id, request))
                .build();
    }

    /**
     * Xóa user (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UUID> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    /**
     * Vô hiệu hóa user (Admin)
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> disableUser(@PathVariable UUID id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.disableUser(id))
                .build();
    }

    /**
     * Kích hoạt lại user (Admin)
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> enableUser(@PathVariable UUID id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.enableUser(id))
                .build();
    }

    /**
     * Gán role cho user (Admin)
     */
    @PostMapping("/{id}/assign-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> assignRoleToUser(@PathVariable UUID id,
                                                      @RequestParam PredefinedRole roleName) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.assignRoleToUser(id, roleName))
                .build();
    }

    /**
     * Xóa role của user (Admin)
     */
    @DeleteMapping("/{id}/remove-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserResponse> removeRoleFromUser(@PathVariable UUID id,
                                                        @RequestParam PredefinedRole roleName) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.removeRoleFromUser(id, roleName))
                .build();
    }

    // ============ COACHES ============

    /**
     * Lấy danh sách tất cả coaches (public endpoint)
     * GET /api/user/coaches
     */
    @GetMapping("/coaches")
    @PreAuthorize("permitAll()")
    public ApiResponse<java.util.List<UserResponse>> getAllCoaches() {
        return ApiResponse.<java.util.List<UserResponse>>builder()
                .result(userService.getAllCoaches())
                .build();
    }

}
