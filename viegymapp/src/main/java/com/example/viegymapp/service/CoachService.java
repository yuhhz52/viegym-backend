package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.AddClientRequest;
import com.example.viegymapp.dto.request.AssignProgramRequest;
import com.example.viegymapp.dto.request.WithdrawRequest;
import com.example.viegymapp.dto.response.ClientResponse;
import com.example.viegymapp.dto.response.CoachBalanceResponse;
import com.example.viegymapp.dto.response.CoachStatsResponse;
import com.example.viegymapp.dto.response.WorkoutProgramResponse;

import java.util.List;
import java.util.UUID;

public interface CoachService {
    
    /**
     * Get statistics for the current coach
     */
    CoachStatsResponse getCoachStats();
    
    /**
     * Get all clients of the current coach
     */
    List<ClientResponse> getMyClients();
    
    /**
     * Get active clients of the current coach
     */
    List<ClientResponse> getActiveClients();
    
    /**
     * Get a specific client by ID
     */
    ClientResponse getClientById(UUID clientId);
    
    /**
     * Add a new client to the coach's roster
     */
    ClientResponse addClient(AddClientRequest request);
    
    /**
     * Remove a client from the coach's roster
     */
    void removeClient(UUID clientId);
    
    /**
     * Update client notes
     */
    ClientResponse updateClientNotes(UUID clientId, String notes);
    
    /**
     * Get all programs created by the current coach
     */
    List<WorkoutProgramResponse> getMyPrograms();
    
    /**
     * Assign a program to a client
     */
    void assignProgramToClient(AssignProgramRequest request);
    
    /**
     * Get programs assigned to a specific client
     */
    List<WorkoutProgramResponse> getClientPrograms(UUID clientId);
    
    /**
     * Get coach's balance information
     */
    CoachBalanceResponse getCoachBalance();
    
    /**
     * Request withdrawal
     */
    CoachBalanceResponse withdraw(WithdrawRequest request);
    
    /**
     * Process all completed bookings that still have pending transactions
     * This is a recovery method to fix bookings that were completed before the fix
     */
    int processPendingCompletedBookings();
}
