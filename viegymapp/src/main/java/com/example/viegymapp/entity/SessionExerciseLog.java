package com.example.viegymapp.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import java.util.UUID;

@Entity
@Table(name = "session_exercise_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class SessionExerciseLog extends BaseEntity{
     @Id
    @UuidGenerator
    @Column(name = "log_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "set_number")
    private Integer setNumber;

    @Column(name = "reps_done")
    private Integer repsDone;

    @Column(name = "weight_used")
    private Double weightUsed;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    @Column(name = "body_weight")
    private Double bodyWeight;

    @Column(name = "set_notes")
    private String setNotes;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    /**
     * Tính volume dựa trên loại bài tập
     */
    public double calculateVolume() {
        if (exercise == null) {
            return (weightUsed != null ? weightUsed : 0.0) * 
                   (repsDone != null ? repsDone : 0);
        }
        
        if (exercise.getExerciseType() == null) {
            return (weightUsed != null ? weightUsed : 0.0) * 
                   (repsDone != null ? repsDone : 0);
        }

        switch (exercise.getExerciseType()) {
            case WEIGHT_AND_REPS:
                return (weightUsed != null ? weightUsed : 0.0) * 
                       (repsDone != null ? repsDone : 0);

            case BODYWEIGHT_REPS:
                // Body weight exercises: volume = (bodyWeight + weightUsed) × reps
                // bodyWeight: body weight của người tập (ví dụ: 70kg)
                // weightUsed: weight thêm vào (ví dụ: +10kg cho weighted pull-ups)
                double totalWeight = (bodyWeight != null ? bodyWeight : 0.0) + 
                                    (weightUsed != null ? weightUsed : 0.0);
                // Nếu không có bodyWeight và weightUsed, dùng bodyWeight mặc định 70kg
                if (totalWeight == 0.0) {
                    totalWeight = 70.0; // Body weight mặc định
                }
                return totalWeight * (repsDone != null ? repsDone : 0);

            case ASSISTED_BODYWEIGHT:
                // Body weight - assist weight
                double effectiveWeight = (bodyWeight != null ? bodyWeight : 0.0) - 
                                       (weightUsed != null ? weightUsed : 0.0);
                return Math.max(0, effectiveWeight) * (repsDone != null ? repsDone : 0);

            case REPS_ONLY:
            case TIME_BASED:
            case DISTANCE_BASED:
            case WEIGHT_AND_TIME:
            default:
                return 0.0; // Không tính volume
        }
    }

    /**
     * Get display value cho set này
     */
    public String getDisplayValue() {
        if (exercise == null) {
            return String.format("%.1f kg × %d", 
                weightUsed != null ? weightUsed : 0.0,
                repsDone != null ? repsDone : 0);
        }
        
        if (exercise.getExerciseType() == null) {
            return String.format("%.1f kg × %d", 
                weightUsed != null ? weightUsed : 0.0,
                repsDone != null ? repsDone : 0);
        }

        switch (exercise.getExerciseType()) {
            case WEIGHT_AND_REPS:
                return String.format("%.1f kg × %d", 
                    weightUsed != null ? weightUsed : 0.0,
                    repsDone != null ? repsDone : 0);

            case BODYWEIGHT_REPS:
                return String.format("%d reps (%.1f kg)", 
                    repsDone != null ? repsDone : 0,
                    bodyWeight != null ? bodyWeight : 0.0);

            case REPS_ONLY:
                return String.format("%d reps", repsDone != null ? repsDone : 0);

            case TIME_BASED:
                int minutes = (durationSeconds != null ? durationSeconds : 0) / 60;
                int seconds = (durationSeconds != null ? durationSeconds : 0) % 60;
                return String.format("%d:%02d", minutes, seconds);

            case DISTANCE_BASED:
                return String.format("%.2f m", distanceMeters != null ? distanceMeters : 0.0);

            case WEIGHT_AND_TIME:
                int mins = (durationSeconds != null ? durationSeconds : 0) / 60;
                int secs = (durationSeconds != null ? durationSeconds : 0) % 60;
                return String.format("%.1f kg × %d:%02d", 
                    weightUsed != null ? weightUsed : 0.0, mins, secs);

            case ASSISTED_BODYWEIGHT:
                return String.format("%d reps (-%.1f kg)", 
                    repsDone != null ? repsDone : 0,
                    weightUsed != null ? weightUsed : 0.0);

            default:
                return "-";
        }
    }
}
