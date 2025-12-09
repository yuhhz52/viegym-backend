package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.BookingRequest;
import com.example.viegymapp.dto.request.TimeSlotRequest;
import com.example.viegymapp.dto.response.BookingResponse;
import com.example.viegymapp.dto.response.BookingNotificationResponse;
import com.example.viegymapp.dto.response.TimeSlotResponse;
import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.CoachTimeSlot;
import com.example.viegymapp.entity.Notification;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.BookingMapper;
import com.example.viegymapp.mapper.TimeSlotMapper;
import com.example.viegymapp.repository.BookingSessionRepository;
import com.example.viegymapp.repository.CoachTimeSlotRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.AsyncNotificationService;
import com.example.viegymapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CoachTimeSlotRepository timeSlotRepository;
    private final BookingSessionRepository bookingRepository;
    private final UserRepository userRepository;
    private final TimeSlotMapper timeSlotMapper;
    private final BookingMapper bookingMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final AsyncNotificationService asyncNotificationService;

    @Override
    public TimeSlotResponse createTimeSlot(TimeSlotRequest request) {
        User currentUser = getCurrentUser();
        // Verify user is a coach
        boolean isCoach = currentUser.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName() == PredefinedRole.ROLE_COACH);
        if (!isCoach) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Check for overlapping time slots
        boolean hasOverlap = timeSlotRepository.existsOverlappingSlot(
            currentUser.getId(),
            request.getStartTime(),
            request.getEndTime()
        );
        if (hasOverlap) {
            throw new AppException(ErrorCode.TIMESLOT_OVERLAPS);
        }
        CoachTimeSlot timeSlot = timeSlotMapper.toEntity(request);
        timeSlot.setCoach(currentUser);
        
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.toResponse(timeSlot);
    }
    
    @Override
    public List<TimeSlotResponse> getMyTimeSlots() {
        User currentUser = getCurrentUser();
        List<CoachTimeSlot> slots = timeSlotRepository.findByCoach(currentUser);
        return slots.stream()
            .map(timeSlotMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TimeSlotResponse> getAvailableSlotsByCoach(UUID coachId) {
        List<CoachTimeSlot> slots = timeSlotRepository.findAvailableSlotsByCoach(coachId, LocalDateTime.now());
        return slots.stream()
            .map(timeSlotMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TimeSlotResponse> getAllAvailableSlots() {
        List<CoachTimeSlot> slots = timeSlotRepository.findAllAvailableSlots(LocalDateTime.now());
        return slots.stream()
            .map(timeSlotMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public TimeSlotResponse updateTimeSlot(UUID slotId, TimeSlotRequest request) {
        User currentUser = getCurrentUser();
        
        CoachTimeSlot timeSlot = timeSlotRepository.findById(slotId)
            .orElseThrow(() -> new AppException(ErrorCode.TIMESLOT_NOT_FOUND));
        
        // Verify ownership
        if (!timeSlot.getCoach().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Check for overlapping time slots (excluding current slot)
        boolean hasOverlap = timeSlotRepository.existsOverlappingSlotExcludingId(
            currentUser.getId(),
            slotId,
            request.getStartTime(),
            request.getEndTime()
        );
        
        if (hasOverlap) {
            throw new AppException(ErrorCode.TIMESLOT_OVERLAPS);
        }
        
        timeSlotMapper.updateEntity(request, timeSlot);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.toResponse(timeSlot);
    }
    
    @Override
    public void deleteTimeSlot(UUID slotId) {
        User currentUser = getCurrentUser();
        
        CoachTimeSlot timeSlot = timeSlotRepository.findById(slotId)
            .orElseThrow(() -> new AppException(ErrorCode.TIMESLOT_NOT_FOUND));
        
        // Verify ownership
        if (!timeSlot.getCoach().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Check if there are any active bookings for this slot
        boolean hasActiveBookings = bookingRepository.existsByTimeSlot(timeSlot);
        
        if (hasActiveBookings) {
            // Cannot delete slot with existing bookings
            throw new AppException(ErrorCode.SLOT_HAS_BOOKINGS);
        }
        
        // Safe to delete - no active bookings exist
        timeSlotRepository.delete(timeSlot);
    }
    
    @Override
    @Transactional
    public synchronized BookingResponse createBooking(BookingRequest request) {
        User currentUser = getCurrentUser();
        
        // Anti-spam: Check booking rate limit (max 5 bookings per 10 minutes)
        OffsetDateTime tenMinutesAgo = OffsetDateTime.now().minusMinutes(10);
        long recentBookings = bookingRepository.countByClientAndCreatedAtAfter(currentUser, tenMinutesAgo);
        if (recentBookings >= 5) {
            throw new AppException(ErrorCode.TOO_MANY_BOOKINGS);
        }
        
        // Get time slot
        CoachTimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
            .orElseThrow(() -> new AppException(ErrorCode.TIMESLOT_NOT_FOUND));
        
        // Check if slot is available
        if (!timeSlot.getIsAvailable() || timeSlot.getStatus() != CoachTimeSlot.SlotStatus.AVAILABLE) {
            throw new AppException(ErrorCode.TIMESLOT_NOT_AVAILABLE);
        }
        
        // Check if there's already a booking for this time slot
        if (bookingRepository.existsByTimeSlot(timeSlot)) {
            throw new AppException(ErrorCode.TIMESLOT_NOT_AVAILABLE);
        }
        
        // Get coach
        User coach = userRepository.findById(request.getCoachId())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Prevent coach from booking themselves
        if (coach.getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.CANNOT_BOOK_SELF);
        }
        
        // Create booking
        BookingSession booking = bookingMapper.toEntity(request);
        booking.setCoach(coach);
        booking.setClient(currentUser);
        booking.setTimeSlot(timeSlot);
        booking.setBookingTime(timeSlot.getStartTime());
        
        // Mark slot as booked
        timeSlot.setIsAvailable(false);
        timeSlot.setStatus(CoachTimeSlot.SlotStatus.BOOKED);
        timeSlotRepository.save(timeSlot);
        
        booking = bookingRepository.save(booking);
        
        // Send real-time notification to coach
        sendBookingNotificationToCoach(booking, "NEW_BOOKING");
        
        // Send notification to client (user who booked) - ASYNC via RabbitMQ
        String timeSlotInfo = timeSlot.getStartTime().toString() + " - " + timeSlot.getEndTime().toString();
        asyncNotificationService.publishBookingNotification(
            currentUser.getId(),
            coach.getFullName(),
            timeSlotInfo,
            Notification.NotificationType.BOOKING_CONFIRMED
        );
        
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public List<BookingResponse> getMyBookings() {
        User currentUser = getCurrentUser();
        List<BookingSession> bookings = bookingRepository.findByClient(currentUser);
        return bookings.stream()
            .map(bookingMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getCoachBookings() {
        User currentUser = getCurrentUser();
        List<BookingSession> bookings = bookingRepository.findByCoach(currentUser);
        return bookings.stream()
            .map(bookingMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public BookingResponse getBookingById(UUID bookingId) {
        User currentUser = getCurrentUser();
        
        BookingSession booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // Verify user is coach or client of this booking
        if (!booking.getCoach().getId().equals(currentUser.getId()) && 
            !booking.getClient().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public BookingResponse confirmBooking(UUID bookingId) {
        User currentUser = getCurrentUser();
        
        BookingSession booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // Only coach can confirm
        if (!booking.getCoach().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        booking.setStatus(BookingSession.BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public BookingResponse cancelBooking(UUID bookingId) {
        User currentUser = getCurrentUser();
        // Anti-spam: Check cancellation rate limit (max 10 cancellations per hour)
        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
        long recentCancellations = bookingRepository.countByClientAndStatusAndUpdatedAtAfter(
            currentUser, 
            BookingSession.BookingStatus.CANCELLED, 
            oneHourAgo
        );
        if (recentCancellations >= 10) {
            throw new AppException(ErrorCode.TOO_MANY_CANCELLATIONS);
        }
        
        BookingSession booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // Both coach and client can cancel
        if (!booking.getCoach().getId().equals(currentUser.getId()) && 
            !booking.getClient().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        booking.setStatus(BookingSession.BookingStatus.CANCELLED);
        
        // Free up the time slot
        if (booking.getTimeSlot() != null) {
            CoachTimeSlot timeSlot = booking.getTimeSlot();
            timeSlot.setIsAvailable(true);
            timeSlot.setStatus(CoachTimeSlot.SlotStatus.AVAILABLE);
            timeSlotRepository.save(timeSlot);
        }
        
        booking = bookingRepository.save(booking);
        
        // Send cancellation notification to coach if cancelled by client
        if (booking.getClient().getId().equals(currentUser.getId())) {
            sendBookingNotificationToCoach(booking, "BOOKING_CANCELLED");
        }
        
        // Send notification to the user who cancelled - ASYNC via RabbitMQ
        String timeSlotInfo = booking.getTimeSlot() != null 
            ? booking.getTimeSlot().getStartTime().toString() + " - " + booking.getTimeSlot().getEndTime().toString()
            : "Lịch đã đặt";
        String coachName = booking.getCoach().getFullName();
        
        asyncNotificationService.publishBookingNotification(
            currentUser.getId(),
            coachName,
            timeSlotInfo,
            Notification.NotificationType.BOOKING_CANCELLED
        );
        
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public BookingResponse completeBooking(UUID bookingId, String coachNotes) {
        User currentUser = getCurrentUser();
        
        BookingSession booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        // Only coach can complete
        if (!booking.getCoach().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        booking.setStatus(BookingSession.BookingStatus.COMPLETED);
        booking.setCoachNotes(coachNotes);
        booking = bookingRepository.save(booking);
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public List<BookingResponse> getBookingsByDateRange(LocalDateTime start, LocalDateTime end) {
        User currentUser = getCurrentUser();
        List<BookingSession> bookings = bookingRepository.findBookingsByUserAndDateRange(
            currentUser.getId(), start, end);
        return bookings.stream()
            .map(bookingMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<com.example.viegymapp.dto.response.ClientSummaryResponse> getMyClients() {
        User currentCoach = getCurrentUser();
        
        // Get all confirmed bookings for this coach
        List<BookingSession> bookings = bookingRepository.findByCoachId(currentCoach.getId());
        
        // Extract unique clients
        return bookings.stream()
            .map(BookingSession::getClient)
            .distinct()
            .map(client -> com.example.viegymapp.dto.response.ClientSummaryResponse.builder()
                .id(client.getId())
                .email(client.getEmail())
                .fullName(client.getFullName())
                .avatarUrl(client.getAvatarUrl())
                .build())
            .collect(Collectors.toList());
    }
    
    private void sendBookingNotificationToCoach(BookingSession booking, String type) {
        try {
            // Create notification message
            BookingNotificationResponse notification = BookingNotificationResponse.builder()
                .bookingId(booking.getId())
                .coachId(booking.getCoach().getId())
                .clientName(booking.getClient().getFullName())
                .clientEmail(booking.getClient().getEmail())
                .bookingTime(booking.getBookingTime())
                .timeSlotInfo(String.format("%s - %s", 
                    booking.getTimeSlot().getStartTime().toString(),
                    booking.getTimeSlot().getEndTime().toString()))
                .message(String.format("Bạn có lịch hẹn mới từ %s", booking.getClient().getFullName()))
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();
            
            // Send to specific coach via WebSocket
            String destination = "/topic/coach/" + booking.getCoach().getId() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            // Also send to general coach notification channel
            messagingTemplate.convertAndSend("/topic/coach/bookings", notification);
            
        } catch (Exception e) {
            // Log error but don't fail the booking operation
            // You may want to log this exception using a logger
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}