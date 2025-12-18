package com.example.viegymapp.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Direct email service as fallback when RabbitMQ is unavailable
 */
@Service
@Slf4j
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${viegym.app.frontendUrl:http://localhost:5173}")
    private String frontendUrl;

    private String normalizeFrontendUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("Frontend URL is null or empty, using default: http://localhost:5173");
            return "http://localhost:5173";
        }
        
        String cleaned = url.trim();

        try {
            if (cleaned.contains("%") && !cleaned.contains("://")) {
                String decoded = java.net.URLDecoder.decode(cleaned, java.nio.charset.StandardCharsets.UTF_8);
                log.info("Decoded frontend URL from: {} to: {}", cleaned, decoded);
                cleaned = decoded;
            } else if (cleaned.contains("%") && cleaned.contains("://")) {
                int queryIndex = cleaned.indexOf('?');
                if (queryIndex == -1) {
                    // Không có query string, kiểm tra xem có cần decode không
                    // Nếu URL có % nhưng không phải là ký tự hợp lệ trong URL (như %20 cho space)
                    // thì có thể đã bị encode sai
                    if (cleaned.matches(".*%[0-9A-Fa-f]{2}.*") && !cleaned.matches("https?://[^%]+")) {
                        String decoded = java.net.URLDecoder.decode(cleaned, java.nio.charset.StandardCharsets.UTF_8);
                        log.info("Decoded frontend URL (with protocol) from: {} to: {}", cleaned, decoded);
                        cleaned = decoded;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to decode URL, using original: {} - Error: {}", cleaned, e.getMessage());
        }
        
        // Loại bỏ trailing slash
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        // Validate URL format
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            log.warn("Frontend URL does not start with http:// or https://, adding http://");
            cleaned = "http://" + cleaned;
        }
        
        return cleaned;
    }
    
    /**
     * Send password reset email directly without queue
     * Used as fallback when RabbitMQ is unavailable
     */
    public void sendPasswordResetEmailDirect(String toEmail, String resetToken) {
        log.info("Sending password reset email directly (fallback mode) to: {}", toEmail);
        
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email sending is disabled.");
            return;
        }
        
        try {
            // Normalize frontend URL để xử lý cả local và production
            String cleanFrontendUrl = normalizeFrontendUrl(frontendUrl);
            
            log.info("Frontend URL (normalized): {}", cleanFrontendUrl);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("VieGym - Đặt lại mật khẩu");
            
            // UUID token không chứa ký tự đặc biệt, nhưng encode để an toàn
            // Chỉ encode token, không encode toàn bộ URL
            String encodedToken = java.net.URLEncoder.encode(resetToken, java.nio.charset.StandardCharsets.UTF_8);
            String resetLink = cleanFrontendUrl + "/auth/reset-password?token=" + encodedToken;
            
            log.info("Generated reset link: {}", resetLink);
            
            String htmlContent = buildPasswordResetEmailTemplate(resetLink);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent directly to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email directly to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
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
                        <div class="logo">VieGym</div>
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
                            <strong>Lưu ý:</strong>
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

