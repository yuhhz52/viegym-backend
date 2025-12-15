package com.example.viegymapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",  // Web frontend local
            "http://localhost:8081",  // Expo web/mobile
            "http://localhost:8082",  // Expo alternate port
            "http://192.168.*.*:*",   // Local network (for mobile devices)
            "https://*.ngrok-free.dev", // Ngrok tunnel
            "https://viegym.netlify.app",
            "https://*.onrender.com"  // Render backend
        ));

        // Cho phép gửi cookie & header xác thực
        config.setAllowCredentials(true);

        // Cho phép tất cả các method HTTP
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cho phép tất cả header từ client gửi lên
        config.setAllowedHeaders(Arrays.asList("*"));

        // Cho phép client đọc các header này trong response
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Set-Cookie"
        ));

        // Cache preflight request trong 1h
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
