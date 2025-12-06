package com.example.viegymapp.entity;
import com.example.viegymapp.entity.Enum.MealType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "nutrition_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "log_date", "meal_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLog extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "nutrition_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private MealType mealType;

    private Integer calories;

    @Column(name = "protein_g")
    private Integer proteinG;

    @Column(name = "carbs_g")
    private Integer carbsG;

    @Column(name = "fats_g")
    private Integer fatsG;
    
    private String notes;
    
}
