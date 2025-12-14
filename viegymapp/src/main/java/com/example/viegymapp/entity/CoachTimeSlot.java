package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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

    @Column(precision = 10, scale = 2)
    BigDecimal price;
    
    @Column(nullable = false)
    Integer capacity = 1; // Default 1 for 1-1 PT, >1 for group classes
    
    @Column(nullable = false)
    Integer bookedCount = 0; // Number of confirmed bookings for this slot

    public enum SlotStatus {
        AVAILABLE,
        FULL,    // When bookedCount >= capacity
        DISABLED // Coach disabled this slot
    }
}
