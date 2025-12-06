package com.example.viegymapp.entity;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "saved_programs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"program_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedProgram extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "saved_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private WorkoutProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
