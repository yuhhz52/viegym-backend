package com.example.viegymapp.service;

import com.example.viegymapp.entity.PasswordResetToken;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.PasswordResetTokenRepository;
import com.example.viegymapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducerService emailProducerService;
    private static final long TOKEN_EXPIRATION_MS = 60 * 60 * 1000;
    
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Kiểm tra xem có token chưa hết hạn và chưa sử dụng không
        Instant now = Instant.now();
        passwordResetTokenRepository.findByUserAndIsUsedFalseAndExpiryDateAfter(user, now)
                .ifPresent(existingToken -> {
                    throw new AppException(ErrorCode.TOO_MANY_RESET_REQUESTS);
                });
        
        // Xóa các token cũ đã hết hạn hoặc đã sử dụng của user
        passwordResetTokenRepository.deleteByUser(user);
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(TOKEN_EXPIRATION_MS);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .isUsed(false)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        // Gửi email qua RabbitMQ queue
        emailProducerService.sendPasswordResetEmail(email, token);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));
        
        // Kiểm tra token đã hết hạn chưa
        if (resetToken.isExpired()) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        
        // Kiểm tra token đã được sử dụng chưa
        if (resetToken.isUsed()) {
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
        }
        
        // Cập nhật mật khẩu mới
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Đánh dấu token đã được sử dụng
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
