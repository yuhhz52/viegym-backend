package com.example.viegymapp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${viegym.app.jwtSecret}")
    private String jwtSecret;

    @Value("${viegym.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${viegym.app.jwtCookieName}")
    private String jwtCookie;

    @Value("${viegym.app.jwtRefreshCookieName}")
    private String jwtRefreshCookie;

    @Value("${viegym.app.jwtRefreshExpirationMs}")
    private Long jwtRefreshExpirationMs;

    @Value("${viegym.app.cookieSecure:true}")
    private boolean cookieSecure;

    //Tạo JWT từ userName
    public String generateTokenFromUsername(String userName, List<String> roles) {
        List<String> roleClaims = roles == null ? Collections.emptyList() : new ArrayList<>(roles);
        return Jwts.builder()
                .setSubject(userName)
                .claim("roles", roleClaims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    //Sinh key từ secret
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //Sinh access token cookie
    public ResponseCookie generateJwtCookie(String userName, List<String> roles) {
        String jwt = generateTokenFromUsername(userName, roles);
        return generateCookie(jwtCookie, jwt, false);
    }

    //Sinh access token cookie với rememberMe
    public ResponseCookie generateJwtCookie(String userName, List<String> roles, boolean rememberMe) {
        String jwt = generateTokenFromUsername(userName, roles);
        return generateCookie(jwtCookie, jwt, rememberMe);
    }

    //Sinh refresh token cookie
    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        long maxAgeSeconds = jwtRefreshExpirationMs != null ? jwtRefreshExpirationMs / 1000 : 24 * 60 * 60;
        return ResponseCookie.from(jwtRefreshCookie, refreshToken)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
    }

    //Sinh refresh token cookie với rememberMe
    public ResponseCookie generateRefreshJwtCookie(String refreshToken, boolean rememberMe) {
        long maxAgeSeconds;
        if (rememberMe) {
            // Remember me: sử dụng thời gian refresh token (7 ngày)
            maxAgeSeconds = jwtRefreshExpirationMs != null ? jwtRefreshExpirationMs / 1000 : 7 * 24 * 60 * 60;
        } else {
            // Session cookie: -1 = hết hạn khi đóng browser
            maxAgeSeconds = -1;
        }
        return ResponseCookie.from(jwtRefreshCookie, refreshToken)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
    }

    // Lấy Token từ cookie
    public String getJwtFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtCookie);
    }
    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtRefreshCookie);
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .maxAge(0)
                .build();
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(jwtRefreshCookie, "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .maxAge(0)
                .build();
    }


    //Lấy username từ JWT
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?>) {
            List<?> rawRoles = (List<?>) rolesClaim;
            List<String> roles = new ArrayList<>();
            for (Object role : rawRoles) {
                if (role instanceof String) {
                    roles.add((String) role);
                }
            }
            return roles;
        }
        return Collections.emptyList();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private ResponseCookie generateCookie(String name, String value, boolean rememberMe) {
        long maxAge = rememberMe ? (24 * 60 * 60) : -1; // 24h nếu remember, session nếu không
        return ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
    }

    //lấy giá trị cookie theo tên
    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }
}
