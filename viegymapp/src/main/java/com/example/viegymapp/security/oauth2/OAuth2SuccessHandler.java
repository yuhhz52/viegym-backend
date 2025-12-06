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
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user.getEmail(), roles);
        ResponseCookie refreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        response.addHeader("Set-Cookie", jwtCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        String requestUrl = request.getRequestURL().toString();
        String state = request.getParameter("state");
        String mobileState = request.getParameter("mobileState");
        
        // Try to get mobileState from session if not in parameter
        if (mobileState == null || mobileState.isEmpty()) {
            mobileState = (String) request.getSession().getAttribute("mobileState");
        }
        
        // Use mobileState if available, otherwise fall back to state
        String effectiveState = (mobileState != null && !mobileState.isEmpty()) ? mobileState : state;
        
        boolean isMobileRequest = requestUrl.contains("ngrok") || 
                                 (mobileState != null && !mobileState.isEmpty()) ||
                                 (state != null && state.startsWith("mobile_"));
        
        if (isMobileRequest && effectiveState != null && !effectiveState.isEmpty()) {
            // Mobile OAuth - store result for polling
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
            
            MobileAuthController.storeOAuthResult(effectiveState, userInfo);
            
            // Don't redirect, just return success page with auto-close script
            response.setContentType("text/html;charset=UTF-8");
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Authentication Successful</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .card {
                            background: white;
                            padding: 48px 40px;
                            border-radius: 16px;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                            text-align: center;
                            max-width: 400px;
                            width: 90%%;
                        }
                        .checkmark {
                            width: 80px;
                            height: 80px;
                            border-radius: 50%%;
                            background: #10b981;
                            margin: 0 auto 24px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            animation: scaleIn 0.3s ease-out;
                        }
                        @keyframes scaleIn {
                            from { transform: scale(0); }
                            to { transform: scale(1); }
                        }
                        .checkmark svg {
                            width: 48px;
                            height: 48px;
                            stroke: white;
                            stroke-width: 3;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                            fill: none;
                            animation: draw 0.5s ease-out 0.2s forwards;
                            stroke-dasharray: 100;
                            stroke-dashoffset: 100;
                        }
                        @keyframes draw {
                            to { stroke-dashoffset: 0; }
                        }
                        h1 {
                            font-size: 24px;
                            color: #1f2937;
                            margin-bottom: 12px;
                        }
                        p {
                            color: #6b7280;
                            font-size: 16px;
                            margin-bottom: 8px;
                        }
                        .small {
                            font-size: 14px;
                            color: #9ca3af;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="checkmark">
                            <svg viewBox="0 0 52 52">
                                <polyline points="14 27 22 35 38 17"/>
                            </svg>
                        </div>
                        <h1>Xác thực thành công!</h1>
                        <p>Đang quay về ứng dụng...</p>
                        <p class="small">Cửa sổ này sẽ tự động đóng</p>
                    </div>
                    <script>
                        // Try to close window immediately
                        setTimeout(() => { window.close(); }, 100);
                        setTimeout(() => { window.close(); }, 500);
                        setTimeout(() => { window.close(); }, 1000);
                        setTimeout(() => { window.close(); }, 2000);
                    </script>
                </body>
                </html>
                """;
            response.getWriter().write(html);
        } else {
            // Web OAuth - redirect to localhost web frontend
            response.sendRedirect(frontendUrl + "/auth/callback");
        }
    }
}
