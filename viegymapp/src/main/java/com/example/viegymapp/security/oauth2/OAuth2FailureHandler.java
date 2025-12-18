package com.example.viegymapp.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2FailureHandler.class);

    @Value("${viegym.app.frontendUrl:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Normalize and clean frontend URL
     * Handles URL encoding issues and ensures proper format
     */
    private String normalizeFrontendUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.warn("Frontend URL is null or empty, using default: http://localhost:5173");
            return "http://localhost:5173";
        }
        
        String cleaned = url.trim();
        
        // Chỉ decode nếu URL có vẻ đã bị encode (chứa % và không phải là URL hợp lệ)
        try {
            if (cleaned.contains("%") && !cleaned.contains("://")) {
                // Có thể là URL đã bị encode hoàn toàn
                String decoded = java.net.URLDecoder.decode(cleaned, java.nio.charset.StandardCharsets.UTF_8);
                logger.info("Decoded frontend URL from: {} to: {}", cleaned, decoded);
                cleaned = decoded;
            } else if (cleaned.contains("%") && cleaned.contains("://")) {
                // URL có protocol nhưng có thể có phần bị encode
                if (cleaned.matches(".*%[0-9A-Fa-f]{2}.*") && !cleaned.matches("https?://[^%]+")) {
                    String decoded = java.net.URLDecoder.decode(cleaned, java.nio.charset.StandardCharsets.UTF_8);
                    logger.info("Decoded frontend URL (with protocol) from: {} to: {}", cleaned, decoded);
                    cleaned = decoded;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to decode URL, using original: {} - Error: {}", cleaned, e.getMessage());
        }
        
        // Loại bỏ trailing slash
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        // Validate URL format
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            logger.warn("Frontend URL does not start with http:// or https://, adding http://");
            cleaned = "http://" + cleaned;
        }
        
        return cleaned;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        
        // Normalize frontend URL để xử lý cả local và production
        String cleanFrontendUrl = normalizeFrontendUrl(frontendUrl);
        logger.info("Frontend URL (normalized): {}", cleanFrontendUrl);
        
        // Log lỗi để debug
        logger.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        
        // Lấy error message
        String errorMessage = exception.getMessage();
        if (errorMessage == null || errorMessage.isBlank()) {
            errorMessage = "Authentication failed";
        }
        
        // URL encode error message
        String encodedError = java.net.URLEncoder.encode(errorMessage, "UTF-8");
        
        // Redirect về frontend với error message
        logger.info("Redirecting to frontend with error: {}", errorMessage);
        response.sendRedirect(cleanFrontendUrl + "/auth/login?error=" + encodedError);
    }
}

