package com.example.viegymapp.service.Impl;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.repository.BookingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingSessionRepository bookingRepository;

    /**
     * Expire PENDING bookings that have passed their expired_at time (10 minutes)
     * Runs every 2 minutes to check for expired bookings
     */
    @Scheduled(fixedRate = 120000) // 2 minutes in milliseconds
    @Transactional
    public void expirePendingBookings() {
        log.info("Starting expiration check for PENDING bookings...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            List<BookingSession> expiredBookings = bookingRepository.findExpiredPendingBookings(now);
            
            log.info("Found {} expired PENDING bookings", expiredBookings.size());
            
            for (BookingSession booking : expiredBookings) {
                try {
                    // Mark booking as EXPIRED
                    booking.setStatus(BookingSession.BookingStatus.EXPIRED);
                    bookingRepository.save(booking);
                    
                    // Slot is NOT affected - it remains available since booking was never confirmed
                    // booked_count was never incremented for PENDING bookings
                    
                    log.info("Expired booking {} (expired_at: {})", booking.getId(), booking.getExpiredAt());
                            
                } catch (Exception e) {
                    log.error("Error expiring booking {}", booking.getId(), e);
                }
            }
            
            if (expiredBookings.size() > 0) {
                log.info("Expired {} PENDING bookings", expiredBookings.size());
            }
            
        } catch (Exception e) {
            log.error("Error during booking expiration", e);
        }
    }
}