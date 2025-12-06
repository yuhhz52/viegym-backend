package com.example.viegymapp.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.viegymapp.entity.RefreshToken;
import com.example.viegymapp.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    int deleteByUserId(UUID userId);
}
