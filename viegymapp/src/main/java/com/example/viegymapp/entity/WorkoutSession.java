package com.example.viegymapp.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class WorkoutSession extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "session_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    @Column(nullable = false)
    private Instant sessionDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    private String notes;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SessionExerciseLog> logs = new HashSet<>();
}
