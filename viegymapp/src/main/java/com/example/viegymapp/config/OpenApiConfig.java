package com.example.viegymapp.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI viegymAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ViegymApp API Documentation")
                        .description("""
                                📘 REST API cho ứng dụng Viegym - Quản lý tập luyện, dinh dưỡng, sức khỏe.
                                
                                Bao gồm các module:
                                - 🏋️ Workout Sessions (Buổi tập)
                                - 🍽️ Nutrition Logs (Nhật ký ăn uống)
                                - ❤️ Health Logs (Theo dõi sức khỏe)
                                - 🧑‍💻 User Management
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nguyễn Thành Huy")
                                .email("contact@viegym.com")
                                .url("https://viegym.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org"))
                );
    }
}
