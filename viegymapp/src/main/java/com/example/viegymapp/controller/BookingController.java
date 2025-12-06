package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.BookingRequest;
import com.example.viegymapp.dto.request.TimeSlotRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.BookingResponse;
import com.example.viegymapp.dto.response.TimeSlotResponse;
import com.example.viegymapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    // ===== TIME SLOTS =====
    
    @PostMapping("/time-slots")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<TimeSlotResponse> createTimeSlot(@RequestBody TimeSlotRequest request) {
        return ApiResponse.<TimeSlotResponse>builder()
            .result(bookingService.createTimeSlot(request))
            .build();
    }
    
    @GetMapping("/time-slots/my")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<List<TimeSlotResponse>> getMyTimeSlots() {
        return ApiResponse.<List<TimeSlotResponse>>builder()
            .result(bookingService.getMyTimeSlots())
            .build();
    }
    
    @GetMapping("/time-slots/available")
    public ApiResponse<List<TimeSlotResponse>> getAllAvailableSlots() {
        return ApiResponse.<List<TimeSlotResponse>>builder()
            .result(bookingService.getAllAvailableSlots())
            .build();
    }
    
    @GetMapping("/time-slots/coach/{coachId}")
    public ApiResponse<List<TimeSlotResponse>> getAvailableSlotsByCoach(@PathVariable UUID coachId) {
        return ApiResponse.<List<TimeSlotResponse>>builder()
            .result(bookingService.getAvailableSlotsByCoach(coachId))
            .build();
    }
    
    @PutMapping("/time-slots/{slotId}")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<TimeSlotResponse> updateTimeSlot(
            @PathVariable UUID slotId,
            @RequestBody TimeSlotRequest request) {
        return ApiResponse.<TimeSlotResponse>builder()
            .result(bookingService.updateTimeSlot(slotId, request))
            .build();
    }
    
    @DeleteMapping("/time-slots/{slotId}")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<Void> deleteTimeSlot(@PathVariable UUID slotId) {
        bookingService.deleteTimeSlot(slotId);
        return ApiResponse.<Void>builder().build();
    }
    
    // ===== BOOKINGS =====
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return ApiResponse.<BookingResponse>builder()
            .result(bookingService.createBooking(request))
            .build();
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'COACH', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<BookingResponse>> getMyBookings() {
        return ApiResponse.<List<BookingResponse>>builder()
            .result(bookingService.getMyBookings())
            .build();
    }
    
    @GetMapping("/coach")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<List<BookingResponse>> getCoachBookings() {
        return ApiResponse.<List<BookingResponse>>builder()
            .result(bookingService.getCoachBookings())
            .build();
    }
    
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        return ApiResponse.<BookingResponse>builder()
            .result(bookingService.getBookingById(bookingId))
            .build();
    }
    
    @PutMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<BookingResponse> confirmBooking(@PathVariable UUID bookingId) {
        return ApiResponse.<BookingResponse>builder()
            .result(bookingService.confirmBooking(bookingId))
            .build();
    }
    
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable UUID bookingId) {
        return ApiResponse.<BookingResponse>builder()
            .result(bookingService.cancelBooking(bookingId))
            .build();
    }
    
    @PutMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<BookingResponse> completeBooking(
            @PathVariable UUID bookingId,
            @RequestParam(required = false) String coachNotes) {
        return ApiResponse.<BookingResponse>builder()
            .result(bookingService.completeBooking(bookingId, coachNotes))
            .build();
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<List<BookingResponse>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ApiResponse.<List<BookingResponse>>builder()
            .result(bookingService.getBookingsByDateRange(start, end))
            .build();
    }
    
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ROLE_COACH')")
    public ApiResponse<List<com.example.viegymapp.dto.response.ClientSummaryResponse>> getMyClients() {
        return ApiResponse.<List<com.example.viegymapp.dto.response.ClientSummaryResponse>>builder()
            .result(bookingService.getMyClients())
            .build();
    }
}
