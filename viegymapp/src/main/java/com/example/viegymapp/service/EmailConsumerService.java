package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${viegym.app.frontendUrl}")
    private String frontendUrl;
    
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailMessage(EmailMessage emailMessage) {
        log.info("Processing email message for: {}", emailMessage.getToEmail());
        
        try {
            switch (emailMessage.getEmailType()) {
                case PASSWORD_RESET:
                    sendPasswordResetEmail(emailMessage);
                    break;
                case WELCOME:
                    // Future implementation
                    break;
                case NOTIFICATION:
                    // Future implementation
                    break;
            }
            log.info("Email sent successfully to: {}", emailMessage.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", emailMessage.getToEmail(), e);
            throw new RuntimeException("Email sending failed", e); // Will go to DLQ
        }
    }
    
    private void sendPasswordResetEmail(EmailMessage emailMessage) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(emailMessage.getToEmail());
        helper.setSubject(emailMessage.getSubject());
        
        String resetLink = frontendUrl + "/auth/reset-password?token=" + emailMessage.getResetToken();
        String htmlContent = buildPasswordResetEmailTemplate(resetLink);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    private String buildPasswordResetEmailTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #F97316;
                        margin-bottom: 10px;
                    }
                    .content {
                        background-color: white;
                        padding: 25px;
                        border-radius: 8px;
                        margin-bottom: 20px;
                    }
                    .button {
                        display: inline-block;
                        padding: 14px 32px;
                        background-color: #F97316;
                        color: white !important;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: bold;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .button:hover {
                        background-color: #EA580C;
                    }
                    .warning {
                        background-color: #FEF3C7;
                        border-left: 4px solid #F59E0B;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏋️ VieGym</div>
                        <h2 style="color: #333; margin: 0;">Đặt lại mật khẩu</h2>
                    </div>
                    
                    <div class="content">
                        <p>Xin chào,</p>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản VieGym của bạn.</p>
                        <p>Nhấp vào nút bên dưới để đặt lại mật khẩu:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Đặt lại mật khẩu</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Lưu ý:</strong>
                            <ul style="margin: 10px 0 0 0; padding-left: 20px;">
                                <li>Link này chỉ có hiệu lực trong <strong>1 giờ</strong></li>
                                <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
                            </ul>
                        </div>
                        
                        <p style="color: #666; font-size: 14px; margin-top: 20px;">
                            Hoặc copy link sau vào trình duyệt:<br>
                            <code style="background: #f5f5f5; padding: 8px; display: block; margin-top: 8px; word-break: break-all;">%s</code>
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        <p>&copy; 2025 VieGym. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);
    }
}
