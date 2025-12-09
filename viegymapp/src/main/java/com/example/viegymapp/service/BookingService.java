package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.BookingRequest;
import com.example.viegymapp.dto.request.TimeSlotRequest;
import com.example.viegymapp.dto.response.BookingResponse;
import com.example.viegymapp.dto.response.ClientSummaryResponse;
import com.example.viegymapp.dto.response.TimeSlotResponse;
import com.example.viegymapp.entity.BookingSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    
    // Time Slots
    TimeSlotResponse createTimeSlot(TimeSlotRequest request);
    List<TimeSlotResponse> getMyTimeSlots();
    List<TimeSlotResponse> getAvailableSlotsByCoach(UUID coachId);
    List<TimeSlotResponse> getAllAvailableSlots();
    TimeSlotResponse updateTimeSlot(UUID slotId, TimeSlotRequest request);
    void deleteTimeSlot(UUID slotId);
    
    // Bookings
    BookingResponse createBooking(BookingRequest request);
    List<BookingResponse> getMyBookings();
    List<BookingResponse> getCoachBookings();
    BookingResponse getBookingById(UUID bookingId);
    BookingResponse confirmBooking(UUID bookingId);
    BookingResponse cancelBooking(UUID bookingId);
    BookingResponse completeBooking(UUID bookingId, String coachNotes);
    List<BookingResponse> getBookingsByDateRange(LocalDateTime start, LocalDateTime end);
    List<ClientSummaryResponse> getMyClients();
}
