package com.example.viegymapp.entity.Enum;

public enum ExerciseType {
    WEIGHT_AND_REPS("Weight & Reps", "weight_reps", "Weight (kg) x Reps"),
    BODYWEIGHT_REPS("Bodyweight Reps", "bodyweight_reps", "Body Weight x Reps"),
    REPS_ONLY("Reps Only", "reps_only", "Total Reps"),
    TIME_BASED("Time Based", "time_based", "Duration (seconds)"),
    DISTANCE_BASED("Distance Based", "distance_based", "Distance (meters)"),
    WEIGHT_AND_TIME("Weight & Time", "weight_time", "Weight (kg) x Time (seconds)"),
    ASSISTED_BODYWEIGHT("Assisted Bodyweight", "assisted_bodyweight", "Assist Weight x Reps");

    private final String displayName;
    private final String code;
    private final String description;

    ExerciseType(String displayName, String code, String description) {
        this.displayName = displayName;
        this.code = code;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasWeight() {
        return this == WEIGHT_AND_REPS || this == WEIGHT_AND_TIME || this == ASSISTED_BODYWEIGHT;
    }

    public boolean hasReps() {
        return this == WEIGHT_AND_REPS || this == BODYWEIGHT_REPS || 
               this == REPS_ONLY || this == ASSISTED_BODYWEIGHT;
    }

    public boolean hasTime() {
        return this == TIME_BASED || this == WEIGHT_AND_TIME;
    }

    public boolean hasDistance() {
        return this == DISTANCE_BASED;
    }

    public boolean calculateVolume() {
        return this == WEIGHT_AND_REPS || this == BODYWEIGHT_REPS || this == ASSISTED_BODYWEIGHT;
    }
}