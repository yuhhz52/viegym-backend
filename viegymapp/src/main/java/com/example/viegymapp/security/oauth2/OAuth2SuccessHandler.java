package com.example.viegymapp.security.oauth2;

import com.example.viegymapp.controller.MobileAuthController;
import com.example.viegymapp.dto.response.UserInfoResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${viegym.app.frontendUrl:http://localhost:5173}")
    private String frontendUrl;
    
    @Value("${viegym.app.mobileAuthUrl:http://localhost:5173}")
    private String mobileAuthUrl;

    public OAuth2SuccessHandler(JwtUtils jwtUtils, UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            String errorMessage = java.net.URLEncoder.encode("User not found after OAuth2 authentication", "UTF-8");
            response.sendRedirect(frontendUrl + "/auth/login?error=" + errorMessage);
            return;
        }

        User user = userOpt.get();

        // Sinh tokens
        List<String> roles = Optional.ofNullable(user.getUserRoles())
                .orElse(Collections.emptySet())
                .stream()
                .map(userRole -> userRole.getRole().getName().name())
                .collect(Collectors.toList());
        if (roles.isEmpty()) {
            roles = Collections.singletonList("ROLE_USER");
        }

        String accessToken = jwtUtils.generateTokenFromUsername(user.getEmail(), roles);

        // Sau khi xác thực thành công, redirect về FE kèm accessToken trên URL
        String redirectUrl = frontendUrl + "/auth/callback?token=" + accessToken;
        response.sendRedirect(redirectUrl);
    }
}
