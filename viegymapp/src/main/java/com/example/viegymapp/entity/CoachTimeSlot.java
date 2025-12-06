package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coach_time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoachTimeSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    User coach;

    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    @Column(nullable = false)
    Boolean isAvailable = true;

    @Column(length = 500)
    String notes;

    @Column(length = 255)
    String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SlotStatus status = SlotStatus.AVAILABLE;

    public enum SlotStatus {
        AVAILABLE,
        BOOKED,
        CANCELLED
    }
}
