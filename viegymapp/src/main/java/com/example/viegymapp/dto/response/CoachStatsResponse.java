package com.example.viegymapp.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachStatsResponse {
    private Integer totalClients;
    private Integer activeClients;
    private Integer totalPrograms;
    private Integer totalWorkoutsSessions;
    private Double avgClientProgress;
    private Integer newClientsThisMonth;
    private Integer activeProgramsAssigned;
    private Integer completedBookings;
    private Integer cancelledBookings;
    private Integer totalBookings;
}
