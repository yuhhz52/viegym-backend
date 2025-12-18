package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.AddClientRequest;
import com.example.viegymapp.dto.request.AssignProgramRequest;
import com.example.viegymapp.dto.request.WithdrawRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.ClientResponse;
import com.example.viegymapp.dto.response.CoachBalanceResponse;
import com.example.viegymapp.dto.response.CoachStatsResponse;
import com.example.viegymapp.dto.response.WorkoutProgramResponse;
import com.example.viegymapp.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for coach-specific operations
 * Handles client management, program assignments, and coach statistics
 */
@RestController
@RequestMapping("api/coach")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_COACH')")
public class CoachController {

    private final CoachService coachService;

    /**
     * Get coach statistics and dashboard data
     * GET /api/coach/stats
     */
    @GetMapping("/stats")
    public ApiResponse<CoachStatsResponse> getCoachStats() {
        return ApiResponse.<CoachStatsResponse>builder()
                .result(coachService.getCoachStats())
                .build();
    }

    /**
     * Get all clients of the current coach
     * GET /api/coach/clients
     */
    @GetMapping("/clients")
    public ApiResponse<List<ClientResponse>> getMyClients() {
        return ApiResponse.<List<ClientResponse>>builder()
                .result(coachService.getMyClients())
                .build();
    }

    /**
     * Get active clients only
     * GET /api/coach/clients/active
     */
    @GetMapping("/clients/active")
    public ApiResponse<List<ClientResponse>> getActiveClients() {
        return ApiResponse.<List<ClientResponse>>builder()
                .result(coachService.getActiveClients())
                .build();
    }

    /**
     * Get a specific client by ID
     * GET /api/coach/clients/{clientId}
     */
    @GetMapping("/clients/{clientId}")
    public ApiResponse<ClientResponse> getClientById(@PathVariable UUID clientId) {
        return ApiResponse.<ClientResponse>builder()
                .result(coachService.getClientById(clientId))
                .build();
    }

    /**
     * Add a new client to the coach's roster
     * POST /api/coach/clients
     */
    @PostMapping("/clients")
    public ApiResponse<ClientResponse> addClient(@Valid @RequestBody AddClientRequest request) {
        return ApiResponse.<ClientResponse>builder()
                .result(coachService.addClient(request))
                .build();
    }

    /**
     * Remove a client from the coach's roster
     * DELETE /api/coach/clients/{clientId}
     */
    @DeleteMapping("/clients/{clientId}")
    public ApiResponse<Void> removeClient(@PathVariable UUID clientId) {
        coachService.removeClient(clientId);
        return ApiResponse.<Void>builder()
                .message("Đã xóa học viên thành công")
                .build();
    }

    /**
     * Update notes for a specific client
     * PATCH /api/coach/clients/{clientId}/notes
     */
    @PatchMapping("/clients/{clientId}/notes")
    public ApiResponse<ClientResponse> updateClientNotes(
            @PathVariable UUID clientId,
            @RequestBody String notes) {
        return ApiResponse.<ClientResponse>builder()
                .result(coachService.updateClientNotes(clientId, notes))
                .build();
    }

    /**
     * Get all programs created by the current coach
     * GET /api/coach/programs
     */
    @GetMapping("/programs")
    public ApiResponse<List<WorkoutProgramResponse>> getMyPrograms() {
        return ApiResponse.<List<WorkoutProgramResponse>>builder()
                .result(coachService.getMyPrograms())
                .build();
    }

    /**
     * Assign a program to a client
     * POST /api/coach/programs/assign
     */
    @PostMapping("/programs/assign")
    public ApiResponse<Void> assignProgramToClient(@Valid @RequestBody AssignProgramRequest request) {
        coachService.assignProgramToClient(request);
        return ApiResponse.<Void>builder()
                .message("Đã gán chương trình thành công")
                .build();
    }

    /**
     * Get all programs assigned to a specific client
     * GET /api/coach/clients/{clientId}/programs
     */
    @GetMapping("/clients/{clientId}/programs")
    public ApiResponse<List<WorkoutProgramResponse>> getClientPrograms(@PathVariable UUID clientId) {
        return ApiResponse.<List<WorkoutProgramResponse>>builder()
                .result(coachService.getClientPrograms(clientId))
                .build();
    }

    /**
     * Get coach's balance information
     * GET /api/coach/balance
     */
    @GetMapping("/balance")
    public ApiResponse<CoachBalanceResponse> getCoachBalance() {
        return ApiResponse.<CoachBalanceResponse>builder()
                .result(coachService.getCoachBalance())
                .build();
    }

    /**
     * Request withdrawal
     * POST /api/coach/withdraw
     */
    @PostMapping("/withdraw")
    public ApiResponse<CoachBalanceResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ApiResponse.<CoachBalanceResponse>builder()
                .result(coachService.withdraw(request))
                .message("Yêu cầu rút tiền đã được gửi thành công")
                .build();
    }

    /**
     * Process all completed bookings that still have pending transactions
     * This is a recovery endpoint to fix bookings that were completed before the fix
     * POST /api/coach/process-pending-bookings
     */
    @PostMapping("/process-pending-bookings")
    public ApiResponse<Integer> processPendingCompletedBookings() {
        int processedCount = coachService.processPendingCompletedBookings();
        return ApiResponse.<Integer>builder()
                .result(processedCount)
                .message("Đã xử lý " + processedCount + " booking(s) có giao dịch đang chờ")
                .build();
    }
}
