package com.example.viegymapp.repository;

import com.example.viegymapp.entity.PasswordResetToken;
import com.example.viegymapp.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserAndIsUsedFalseAndExpiryDateAfter(User user, Instant now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < ?1")
    void deleteExpiredTokens(Instant now);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
