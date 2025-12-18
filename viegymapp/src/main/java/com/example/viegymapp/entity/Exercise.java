package com.example.viegymapp.entity;

import com.example.viegymapp.entity.Enum.DifficultyLevel;
import com.example.viegymapp.entity.Enum.ExerciseType;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.util.*;

@Entity
@Table(name = "exercises", indexes = {
        @Index(name = "idx_exercises_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity{
       @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "exercise_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "muscle_group")
    private String muscleGroup;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type")
    @Builder.Default
    private ExerciseType exerciseType = ExerciseType.WEIGHT_AND_REPS;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "exercise_tags",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMedia> mediaList;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<ProgramExercise> programExercises = new HashSet<>();

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<SessionExerciseLog> sessionLogs = new HashSet<>();


}
