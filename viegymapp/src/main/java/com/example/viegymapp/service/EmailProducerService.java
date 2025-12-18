package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducerService {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        log.info("Preparing password reset email for: {}", toEmail);
        
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(toEmail)
                .subject("VieGym - Đặt lại mật khẩu")
                .resetToken(resetToken)
                .emailType(EmailMessage.EmailType.PASSWORD_RESET)
                .build();
        
        try {
            log.info("Sending email message to RabbitMQ - Exchange: {}, RoutingKey: {}", 
                    RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                emailMessage
            );
            
            log.info("✅ Password reset email message successfully sent to queue for: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send email message to queue for: {} - Error: {}", 
                    toEmail, e.getMessage(), e);
            // Fallback: In production, you might want to save to database for manual retry
            throw new RuntimeException("Failed to queue password reset email", e);
        }
    }
}
