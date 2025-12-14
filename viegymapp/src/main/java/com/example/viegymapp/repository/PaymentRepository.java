package com.example.viegymapp.repository;

import com.example.viegymapp.entity.Payment;
import com.example.viegymapp.entity.Enum.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByOrderId(String orderId);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByBookingSessionId(UUID bookingSessionId);
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}
