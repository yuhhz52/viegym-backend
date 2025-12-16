package com.example.viegymapp.security.oauth2;

import com.example.viegymapp.controller.MobileAuthController;
import com.example.viegymapp.dto.response.UserInfoResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;

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
        
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Check if this is a mobile OAuth request
        HttpSession session = request.getSession(false);
        String mobileState = (session != null) ? (String) session.getAttribute("mobileState") : null;

        if (mobileState != null && !mobileState.isEmpty()) {
            // Mobile OAuth flow
            System.out.println("[OAuth2SuccessHandler] ✓ Mobile OAuth success for: " + user.getEmail());
            System.out.println("[OAuth2SuccessHandler] ✓ Mobile State: " + mobileState);
            
            // Store tokens for mobile polling
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getEmail())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(new HashSet<>(roles))
                    .avatar(user.getAvatarUrl())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();
            
            MobileAuthController.storeOAuthResult(mobileState, userInfo);
            
            // Clean up session
            session.removeAttribute("mobileState");
            
            // Redirect to mobile success page
            String mobileSuccessUrl = mobileAuthUrl + "/mobile-auth-success?state=" + mobileState;
            System.out.println("[OAuth2SuccessHandler] ✓ Redirecting to: " + mobileSuccessUrl);
            response.sendRedirect(mobileSuccessUrl);
            return;
        }

        // Web OAuth flow
        // URL ENCODE tokens (JWT chứa . và + sẽ bị corrupt nếu không encode)
        String encodedAccessToken = java.net.URLEncoder.encode(accessToken, java.nio.charset.StandardCharsets.UTF_8);
        String encodedRefreshToken = java.net.URLEncoder.encode(refreshToken.getToken(), java.nio.charset.StandardCharsets.UTF_8);

        // THÊM: Set cookie (backup nếu URL param fail)
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(false)  // Cho FE đọc được
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(30 * 60)  // 30 phút
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(false)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)  // 7 ngày
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // Redirect kèm URL params (chính thức) + cookie (backup)
        String redirectUrl = frontendUrl + "/auth/callback?token=" + encodedAccessToken + "&refreshToken=" + encodedRefreshToken;
        
        System.out.println("[OAuth2SuccessHandler] ✓ OAuth2 success for: " + user.getEmail());
        System.out.println("[OAuth2SuccessHandler] ✓ Redirect URL: " + frontendUrl + "/auth/callback?token=***&refreshToken=***");
        System.out.println("[OAuth2SuccessHandler] ✓ Token lengths - Access: " + accessToken.length() + ", Refresh: " + refreshToken.getToken().length());
        System.out.println("[OAuth2SuccessHandler] ✓ Cookies set as backup");
        
        response.sendRedirect(redirectUrl);
    }
}
