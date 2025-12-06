package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.ForgotPasswordRequest;
import com.example.viegymapp.dto.request.LoginRequest;
import com.example.viegymapp.dto.request.ResetPasswordRequest;
import com.example.viegymapp.dto.request.TokenRefreshRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.MessageResponse;
import com.example.viegymapp.dto.response.TokenRefreshResponse;
import com.example.viegymapp.dto.response.UserInfoResponse;
import com.example.viegymapp.service.AuthService;
import com.example.viegymapp.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ApiResponse<UserInfoResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletResponse response) {
        return ApiResponse.<UserInfoResponse>builder()
                .result(authService.login(loginRequest, response))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest request,
                                  HttpServletResponse response) {
        return ApiResponse.<MessageResponse>builder()
                .result(authService.logout(request, response))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @RequestBody(required = false) TokenRefreshRequest refreshRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ApiResponse.<TokenRefreshResponse>builder()
                .result(authService.refreshToken(refreshRequest, request, response))
                .build();
    }
    
    @PostMapping("/forgot-password")
    public ApiResponse<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Tạo token và gửi email qua RabbitMQ queue
        passwordResetService.createPasswordResetToken(request.getEmail());
        
        return ApiResponse.<MessageResponse>builder()
                .result(MessageResponse.builder()
                        .message("Nếu email tồn tại, link đặt lại mật khẩu đã được gửi vào email của bạn")
                        .build())
                .build();
    }
    
    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        
        return ApiResponse.<MessageResponse>builder()
                .result(MessageResponse.builder()
                        .message("Đặt lại mật khẩu thành công")
                        .build())
                .build();
    }
}
