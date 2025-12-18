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
    
    @Value("${viegym.app.cookieSecure:false}")
    private boolean cookieSecure;

    public OAuth2SuccessHandler(JwtUtils jwtUtils, UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Normalize and clean frontend URL
     * Handles URL encoding issues and ensures proper format
     */
    private String normalizeFrontendUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            System.out.println("[OAuth2SuccessHandler] Frontend URL is null or empty, using default: http://localhost:5173");
            return "http://localhost:5173";
        }
        
        String cleaned = url.trim();
        System.out.println("[OAuth2SuccessHandler] Normalizing URL: [" + cleaned + "]");
        
        // Kiểm tra xem URL có hợp lệ không (hỗ trợ cả localhost với port và domain thông thường)
        // Pattern: http://localhost:5173 hoặc https://domain.com hoặc http://127.0.0.1:5173
        boolean isValidUrl = cleaned.matches("^https?://([a-zA-Z0-9.-]+|localhost|127\\.0\\.0\\.1)(:[0-9]+)?(/.*)?$");
        
        if (!isValidUrl) {
            System.out.println("[OAuth2SuccessHandler] URL does not look valid, attempting to decode...");
            
            // Thử decode nếu có chứa %
            try {
                if (cleaned.contains("%")) {
                    String decoded = java.net.URLDecoder.decode(cleaned, java.nio.charset.StandardCharsets.UTF_8);
                    System.out.println("[OAuth2SuccessHandler] Decoded URL from: [" + cleaned + "] to: [" + decoded + "]");
                    cleaned = decoded;
                    // Kiểm tra lại sau khi decode
                    isValidUrl = cleaned.matches("^https?://([a-zA-Z0-9.-]+|localhost|127\\.0\\.0\\.1)(:[0-9]+)?(/.*)?$");
                }
            } catch (Exception e) {
                System.out.println("[OAuth2SuccessHandler] Failed to decode URL: " + e.getMessage());
            }
            
            // Nếu vẫn không hợp lệ sau khi decode, có thể là biến môi trường bị sai
            if (!isValidUrl) {
                System.out.println("[OAuth2SuccessHandler] WARNING: URL still invalid after decode: [" + cleaned + "]");
                System.out.println("[OAuth2SuccessHandler] This might indicate FRONTEND_URL environment variable is set incorrectly!");
                // Chỉ fallback nếu URL thực sự không hợp lệ (không phải localhost)
                // Nếu là localhost hoặc có vẻ là URL hợp lệ, vẫn dùng nó
                if (!cleaned.contains("localhost") && !cleaned.contains("127.0.0.1") && !cleaned.matches(".*\\.[a-z]{2,}.*")) {
                    System.out.println("[OAuth2SuccessHandler] Using default URL: http://localhost:5173");
                    return "http://localhost:5173";
                } else {
                    System.out.println("[OAuth2SuccessHandler] URL seems valid enough, using as-is: [" + cleaned + "]");
                }
            }
        }
        
        // Loại bỏ trailing slash
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        // Validate URL format một lần nữa
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            System.out.println("[OAuth2SuccessHandler] Frontend URL does not start with http:// or https://, adding http://");
            cleaned = "http://" + cleaned;
        }
        
        System.out.println("[OAuth2SuccessHandler] Final normalized URL: [" + cleaned + "]");
        return cleaned;
    }

    @Override
    public void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // Log giá trị gốc để debug
        System.out.println("[OAuth2SuccessHandler] Original frontendUrl from config: [" + frontendUrl + "]");
        System.out.println("[OAuth2SuccessHandler] Original mobileAuthUrl from config: [" + mobileAuthUrl + "]");
        
        // Normalize frontend URL để xử lý cả local và production
        String cleanFrontendUrl = normalizeFrontendUrl(frontendUrl);
        String cleanMobileAuthUrl = normalizeFrontendUrl(mobileAuthUrl);
        
        System.out.println("[OAuth2SuccessHandler] Frontend URL (normalized): [" + cleanFrontendUrl + "]");
        System.out.println("[OAuth2SuccessHandler] Mobile Auth URL (normalized): [" + cleanMobileAuthUrl + "]");

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            String errorMessage = java.net.URLEncoder.encode("User not found after OAuth2 authentication", "UTF-8");
            response.sendRedirect(cleanFrontendUrl + "/auth/login?error=" + errorMessage);
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
            String mobileSuccessUrl = cleanMobileAuthUrl + "/mobile-auth-success?state=" + mobileState;
            System.out.println("[OAuth2SuccessHandler] ✓ Redirecting to: " + mobileSuccessUrl);
            response.sendRedirect(mobileSuccessUrl);
            return;
        }

        // Web OAuth flow
        // Set HttpOnly cookies (secure, không gửi token qua URL)
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user.getEmail(), roles);
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
        
        response.addHeader("Set-Cookie", jwtCookie.toString());
        response.addHeader("Set-Cookie", jwtRefreshCookie.toString());

        // Redirect không kèm token trong URL (an toàn hơn)
        String redirectUrl = cleanFrontendUrl + "/auth/callback";
        
        System.out.println("[OAuth2SuccessHandler] ✓ OAuth2 success for: " + user.getEmail());
        System.out.println("[OAuth2SuccessHandler] ✓ Redirect URL: " + redirectUrl);
        System.out.println("[OAuth2SuccessHandler] ✓ HttpOnly cookies set (secure)");
        System.out.println("[OAuth2SuccessHandler] ✓ Token lengths - Access: " + accessToken.length() + ", Refresh: " + refreshToken.getToken().length());
        
        response.sendRedirect(redirectUrl);
    }
}
