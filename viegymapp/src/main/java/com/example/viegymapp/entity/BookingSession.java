package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }
}
