package com.example.viegymapp.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "health_logs")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class HealthLog extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "health_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant recordedAt;
    
    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "body_fat_percent")
    private Double bodyFatPercent;

    @Column(name = "muscle_mass_kg")
    private Double muscleMassKg;

    @Column(name = "waist_cm")
    private Double waistCm;
    
    @Column(name = "heart_rate")
    private Integer heartRate;
    
    private String note;
}
