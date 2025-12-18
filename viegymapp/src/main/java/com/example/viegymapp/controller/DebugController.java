package com.example.viegymapp.controller;

import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.debug.enabled", havingValue = "true", matchIfMissing = false)
public class DebugController {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.rabbitmq.host:not-configured}")
    private String rabbitHost;
    
    @Value("${spring.rabbitmq.port:not-configured}")
    private String rabbitPort;
    
    /**
     * Test email configuration
     * Usage: POST /api/debug/test-email?to=your-email@example.com
     */
    @PostMapping("/test-email")
    public ApiResponse<MessageResponse> testEmail(@RequestParam String to) {
        log.info("Testing email to: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("VieGym - Test Email");
            message.setText("This is a test email from VieGym backend. If you receive this, email configuration is working correctly.");
            
            if (mailSender != null) {
                mailSender.send(message);
                log.info("Test email sent successfully to: {}", to);
            } else {
                log.warn("JavaMailSender is not configured. Email sending skipped.");
            }
            
            return ApiResponse.<MessageResponse>builder()
                    .result(MessageResponse.builder()
                            .message("Test email sent successfully to: " + to)
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", to, e);
            throw new RuntimeException("Failed to send test email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check mail and RabbitMQ configuration
     * Usage: GET /api/debug/config
     */
    @GetMapping("/config")
    public ApiResponse<MessageResponse> checkConfig() {
        StringBuilder config = new StringBuilder();
        config.append("Mail Configuration:\n");
        config.append("  From Email: ").append(fromEmail).append("\n");
        config.append("\nRabbitMQ Configuration:\n");
        config.append("  Host: ").append(rabbitHost).append("\n");
        config.append("  Port: ").append(rabbitPort).append("\n");
        
        try {
            // Test RabbitMQ connection
            rabbitTemplate.execute(channel -> {
                config.append("  Connection Status: CONNECTED\n");
                return null;
            });
        } catch (Exception e) {
            config.append("  Connection Status: FAILED - ").append(e.getMessage()).append("\n");
        }
        
        log.info("Configuration check:\n{}", config);
        
        return ApiResponse.<MessageResponse>builder()
                .result(MessageResponse.builder()
                        .message(config.toString())
                        .build())
                .build();
    }
    
    /**
     * Check RabbitMQ queues status
     * Usage: GET /api/debug/rabbitmq-status
     */
    @GetMapping("/rabbitmq-status")
    public ApiResponse<MessageResponse> checkRabbitMQStatus() {
        try {
            StringBuilder status = new StringBuilder("RabbitMQ Status:\n");
            
            rabbitTemplate.execute(channel -> {
                try {
                    // Check email queue
                    var emailQueue = channel.queueDeclarePassive("email.queue");
                    status.append("Email Queue:\n");
                    status.append("  Messages: ").append(emailQueue.getMessageCount()).append("\n");
                    status.append("  Consumers: ").append(emailQueue.getConsumerCount()).append("\n");
                    
                    // Check DLQ
                    var dlq = channel.queueDeclarePassive("email.dlq");
                    status.append("Email DLQ:\n");
                    status.append("  Messages: ").append(dlq.getMessageCount()).append("\n");
                } catch (Exception e) {
                    status.append("Error checking queues: ").append(e.getMessage()).append("\n");
                }
                return null;
            });
            
            log.info("RabbitMQ Status:\n{}", status);
            
            return ApiResponse.<MessageResponse>builder()
                    .result(MessageResponse.builder()
                            .message(status.toString())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Failed to check RabbitMQ status", e);
            throw new RuntimeException("Failed to check RabbitMQ status: " + e.getMessage(), e);
        }
    }
}

