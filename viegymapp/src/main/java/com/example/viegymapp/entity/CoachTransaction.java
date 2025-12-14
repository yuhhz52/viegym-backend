package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track all coach financial transactions
 * Records earnings, withdrawals, and refunds
 */
@Entity
@Table(name = "coach_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoachTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_session_id")
    BookingSession bookingSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal platformFee;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal netAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal balanceBefore;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionStatus status;

    @Column(length = 1000)
    String description;

    @Column
    LocalDateTime processedAt;

    public enum TransactionType {
        EARNING,      // Money earned from completed booking
        WITHDRAWAL,   // Money withdrawn to bank
        REFUND,       // Money refunded to customer (deducted from coach)
        ADJUSTMENT    // Manual adjustment by admin
    }

    public enum TransactionStatus {
        PENDING,      // Transaction initiated
        COMPLETED,    // Transaction completed
        FAILED,       // Transaction failed
        CANCELLED     // Transaction cancelled
    }
}
