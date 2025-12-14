package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    CoachTimeSlot timeSlot;

    @Column(nullable = false)
    LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingStatus status = BookingStatus.PENDING;

    @Column(length = 1000)
    String clientNotes;

    @Column(length = 1000)
    String coachNotes;

    @Column(precision = 10, scale = 2)
    BigDecimal amount;

    @Column
    Boolean requiresPayment = false;
    
    @Column
    LocalDateTime expiredAt; // Booking expires if not paid within 10 minutes

    public enum BookingStatus {
        PENDING,    // Created, waiting for payment (10 min expiry)
        CONFIRMED,  // Payment successful
        COMPLETED,  // Session completed
        CANCELLED,  // User/coach cancelled
        EXPIRED,    // PENDING booking expired (not paid in time)
        NO_SHOW     // User didn't show up
    }
}
