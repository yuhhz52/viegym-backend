package com.example.viegymapp.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import com.example.viegymapp.entity.Enum.AuthProvider;
import com.example.viegymapp.entity.Enum.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "body_fat_percent")
    private Double bodyFatPercent;

    @Column(name = "experience_level")
    private String experienceLevel;

    private String goal;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "last_login")
    private Instant lastLogin;

    // Stats
    @Column(columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer streakDays = 0;

    @Column(name = "last_streak_update")
    private LocalDate lastStreakUpdate;

    @Column(columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer totalWorkouts = 0;

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    @Builder.Default
    private Double totalVolume = 0.0;

    // Daily Goals
    @Column(columnDefinition = "INT DEFAULT 8")
    private Integer dailyWaterGoal = 8;

    @Column(columnDefinition = "INT DEFAULT 2000")
    private Integer dailyCalorieGoal = 2000;

    @Column(columnDefinition = "INT DEFAULT 60")
    private Integer dailyWorkoutMins = 60;

    // Settings
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean darkMode = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean notifications = true;

    private String language = "vi";

    // Quan hệ với các entity khác
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exercise> exercises = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private Set<WorkoutProgram> workoutPrograms = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<WorkoutSession> sessions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<NutritionLog> nutritionLogs = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<HealthLog> healthLogs = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<CommunityPost> posts = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<PostComment> comments = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<PostLike> likes = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    // Safe getters to prevent null pointer exceptions
    public Integer getTotalWorkouts() {
        return totalWorkouts != null ? totalWorkouts : 0;
    }

    public Double getTotalVolume() {
        return totalVolume != null ? totalVolume : 0.0;
    }

    public Integer getStreakDays() {
        return streakDays != null ? streakDays : 0;
    }

}
