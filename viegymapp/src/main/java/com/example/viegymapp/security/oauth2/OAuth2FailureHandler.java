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

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        
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
        response.sendRedirect(frontendUrl + "/auth/login?error=" + encodedError);
    }
}

