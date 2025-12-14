package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track coach earnings and balance
 * This tracks money that coaches earn from completed bookings
 */
@Entity
@Table(name = "coach_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoachBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", unique = true, nullable = false)
    User coach;

    // Available balance that can be withdrawn
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    BigDecimal availableBalance = BigDecimal.ZERO;

    // Pending balance (from bookings not yet completed)
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    BigDecimal pendingBalance = BigDecimal.ZERO;

    // Total earned all time
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalEarned = BigDecimal.ZERO;

    // Total withdrawn all time
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Column
    LocalDateTime lastUpdated;

    @Column(length = 500)
    String bankAccountInfo; // For withdrawal purposes
}
