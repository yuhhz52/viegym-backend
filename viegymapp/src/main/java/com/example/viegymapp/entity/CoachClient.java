package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing the relationship between a coach and their client
 */
@Entity
@Table(name = "coach_clients", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"coach_id", "client_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachClient extends BaseEntity {
    
    @Id
    @UuidGenerator
    @Column(name = "coach_client_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "started_date")
    private Instant startedDate;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, COMPLETED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (startedDate == null) {
            startedDate = Instant.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
