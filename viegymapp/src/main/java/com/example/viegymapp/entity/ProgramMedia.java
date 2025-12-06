package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "program_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramMedia extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    private String mediaType;
    private String url;
    private String caption;
    private Integer orderNo;
}
