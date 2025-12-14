package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import com.example.viegymapp.entity.Enum.PaymentMethod;
import com.example.viegymapp.entity.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_session_id", nullable = true) // Allow null for slot payments
    BookingSession bookingSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    User client; // Client for slot payments

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = true)
    User coach; // Coach for slot payments

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true)
    String transactionId; // ID từ MoMo/VNPay

    @Column
    String orderId; // Order ID của hệ thống

    @Column(length = 1000)
    String description;

    @Column
    LocalDateTime paidAt;

    @Column(length = 2000)
    String paymentResponse; // Response JSON từ payment gateway

    @Column(length = 500)
    String failureReason;

    @Column(length = 2000)
    String metadata; // JSON metadata for slot payments
}
