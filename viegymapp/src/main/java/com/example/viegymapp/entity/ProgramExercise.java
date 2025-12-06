package com.example.viegymapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "program_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProgramExercise extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "program_exercise_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private WorkoutProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;


    @Column(name = "day_of_program")
    private Integer dayOfProgram = 1;

    @Column(name = "order_no")
    private Integer orderNo = 0;

    private Integer sets;

    private String reps;

    @Column(name = "weight_scheme")
    private String weightScheme;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    private String notes;
}
