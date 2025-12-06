package com.example.viegymapp.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workout_programs")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorkoutProgram extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "program_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String goal;
    @Column(name = "duration_weeks")
    private Integer durationWeeks;
    
    private String visibility;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    // N-N với Exercise qua ProgramExercise
    @OneToMany(mappedBy = "program")
    private Set<ProgramExercise> programExercises = new HashSet<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgramMedia> mediaList;

    @OneToMany(mappedBy = "program")
    private Set<WorkoutSession> sessions = new HashSet<>();
}
