package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.LoginRequest;
import com.example.viegymapp.dto.request.TokenRefreshRequest;
import com.example.viegymapp.dto.response.MessageResponse;
import com.example.viegymapp.dto.response.TokenRefreshResponse;
import com.example.viegymapp.dto.response.UserInfoResponse;
import com.example.viegymapp.entity.RefreshToken;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.RefreshTokenRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StreakService streakService;
    private final UserRepository userRepository;

    public UserInfoResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        // Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Cập nhật thời gian đăng nhập cuối cùng
        userRepository.findById(userDetails.getId()).ifPresent(user -> {
            user.setLastLogin(Instant.now());
            userRepository.save(user);
        });

        // Tính toán streak khi login
        streakService.calculateAndUpdateStreak(userDetails.getId());

        // Lấy danh sách vai trò của người dùng
        List<String> roleList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        Set<String> roleSet = new HashSet<>(roleList);

        // Tạo token
        String accessToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername(), roleList);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        String refreshTokenStr = refreshToken.getToken();

        // Tạo cookie HttpOnly với rememberMe
        boolean rememberMe = loginRequest.isRememberMe();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails.getUsername(), roleList, rememberMe);
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshTokenStr, rememberMe);

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString());

        // Trả về DTO kèm token với thông tin đầy đủ
        return UserInfoResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .avatar(userDetails.getAvatar())
                .roles(roleSet)
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }


    public MessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        boolean tokenDeleted = false;
        
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(refreshToken);
            if (tokenOptional.isPresent()) {
                refreshTokenService.deleteByUserId(tokenOptional.get().getUser().getId());
                tokenDeleted = true;
            }
        }
        
        if (!tokenDeleted) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                // Xóa tất cả refresh token của user
                refreshTokenService.deleteByUserId(userDetails.getId());
                tokenDeleted = true;
            }
        }

        ResponseCookie cleanJwt = jwtUtils.getCleanJwtCookie();
        ResponseCookie cleanRefresh = jwtUtils.getCleanJwtRefreshCookie();

        response.addHeader(HttpHeaders.SET_COOKIE, cleanJwt.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cleanRefresh.toString());

        return new MessageResponse("Logout successful");
    }


    public TokenRefreshResponse refreshToken(
            TokenRefreshRequest refreshRequest,
            HttpServletRequest request, 
            HttpServletResponse response) {
        // Ưu tiên đọc từ cookie (cho web app)
        String refreshTokenFromCookie = jwtUtils.getJwtRefreshFromCookies(request);
        
        // Nếu không có trong cookie, đọc từ request body (cho mobile app)
        final String refreshToken;
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isEmpty()) {
            refreshToken = refreshTokenFromCookie;
        } else if (refreshRequest != null && refreshRequest.getRefreshToken() != null 
                   && !refreshRequest.getRefreshToken().isEmpty()) {
            refreshToken = refreshRequest.getRefreshToken();
        } else {
            throw new AppException(ErrorCode.TOKEN_REFRESH_FAILED);
        }

        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    try {
                        // Verify expiration
                        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(token);
                        var user = verifiedToken.getUser();
                        
                        List<String> roles = Optional.ofNullable(user.getUserRoles())
                                .orElse(Collections.emptySet())
                                .stream()
                                .map(userRole -> userRole.getRole().getName().name())
                                .collect(Collectors.toList());
                        if (roles.isEmpty()) {
                            roles = Collections.singletonList("ROLE_USER");
                        }
                        
                        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail(), roles);
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user.getEmail(), roles);
                        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

                        // Trả về refresh token (giữ nguyên nếu dùng cookie, hoặc trả về mới nếu từ body)
                        return TokenRefreshResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .build();
                    } catch (AppException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new AppException(ErrorCode.TOKEN_REFRESH_FAILED);
                    }
                })
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_REFRESH_FAILED));
    }

}
