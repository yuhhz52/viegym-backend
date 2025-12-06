package com.example.viegymapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "exercise_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseMedia extends BaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "media_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // IMAGE, VIDEO, MODEL_3D

    @Column(nullable = false)
    private String url;

    private String caption;

    @Column(name = "order_no")
    private Integer orderNo;
}
