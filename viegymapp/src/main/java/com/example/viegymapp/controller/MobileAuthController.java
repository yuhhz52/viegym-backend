package com.example.viegymapp.controller;

import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.UserInfoResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileAuthController {
    
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    
    // Temporary storage for OAuth results (in production, use Redis)
    private static final ConcurrentHashMap<String, UserInfoResponse> oauthResults = new ConcurrentHashMap<>();

    /**
     * Store OAuth result after successful authentication
     */
    public static void storeOAuthResult(String state, UserInfoResponse userInfo) {
        if (state != null && !state.isEmpty()) {
            oauthResults.put(state, userInfo);
            // Auto-expire after 5 minutes
            new Thread(() -> {
                try {
                    Thread.sleep(5 * 60 * 1000);
                    oauthResults.remove(state);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Mobile app polls this endpoint to check if OAuth completed
     */
    @GetMapping("/auth/check-oauth")
    public ResponseEntity<ApiResponse<UserInfoResponse>> checkOAuthResult(@RequestParam String state) {
        UserInfoResponse result = oauthResults.get(state);
        
        if (result != null) {
            // Remove after retrieval
            oauthResults.remove(state);
            
            return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                    .result(result)
                    .message("OAuth login successful")
                    .build());
        }
        
        return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                .code(404)
                .message("OAuth result not found or expired")
                .build());
    }

    @PostMapping("/auth/google-callback")
    public ResponseEntity<ApiResponse<UserInfoResponse>> handleGoogleCallback(HttpServletRequest request) {
        try {
            // Lấy authentication từ security context (sau khi OAuth thành công)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                    .code(401)
                    .message("Not authenticated")
                    .build());
            }

            String email = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                    .code(404)
                    .message("User not found")
                    .build());
            }

            User user = userOpt.get();

            // Tạo JWT token cho mobile
            List<String> roles = Optional.ofNullable(user.getUserRoles())
                    .orElse(Collections.emptySet())
                    .stream()
                    .map(userRole -> userRole.getRole().getName().name())
                    .collect(Collectors.toList());
            
            if (roles.isEmpty()) {
                roles = Collections.singletonList("ROLE_USER");
            }

            String accessToken = jwtUtils.generateTokenFromUsername(user.getEmail(), roles);
            
            // Tạo refresh token
            var refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Tạo response
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getEmail()) // User entity uses email as username
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(new HashSet<>(roles))
                    .avatar(user.getAvatarUrl()) // Correct method name
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();

            return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                    .result(userInfo)
                    .message("Login successful")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<UserInfoResponse>builder()
                    .code(500)
                    .message("Internal server error: " + e.getMessage())
                    .build());
        }
    }
}