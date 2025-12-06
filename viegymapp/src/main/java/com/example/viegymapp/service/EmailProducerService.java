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
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(toEmail)
                .subject("VieGym - Đặt lại mật khẩu")
                .resetToken(resetToken)
                .emailType(EmailMessage.EmailType.PASSWORD_RESET)
                .build();
        
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                emailMessage
            );
            log.info("Password reset email message sent to queue for: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email message to queue for: {}", toEmail, e);
            // Fallback: could save to database for manual retry
        }
    }
}
