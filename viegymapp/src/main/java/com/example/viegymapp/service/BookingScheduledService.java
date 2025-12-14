package com.example.viegymapp.service;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.CoachBalance;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.BookingSessionRepository;
import com.example.viegymapp.repository.CoachBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for booking and payment automation
 * Similar to real-world platforms like Grab, Uber, Upwork
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingScheduledService {

    private final BookingSessionRepository bookingRepository;
    private final CoachBalanceService coachBalanceService;
    private final CoachBalanceRepository coachBalanceRepository;

    /**
     * Auto-complete bookings that have passed their scheduled time
     * Runs every hour
     * 
     * Similar to:
     * - Uber: Auto-complete trip after 24 hours
     * - Upwork: Auto-complete job after 14 days if client doesn't respond
     * - Fiverr: Auto-complete after 3 days of delivery
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void autoCompleteBookings() {
        try {
            // Complete bookings that are 24 hours past their scheduled time
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            
            List<BookingSession> oldBookings = bookingRepository
                    .findByStatusAndBookingTimeBefore(
                            BookingSession.BookingStatus.CONFIRMED, 
                            cutoffTime
                    );
            
            int completedCount = 0;
            for (BookingSession booking : oldBookings) {
                try {
                    log.info("Auto-completing booking {}", booking.getId());
                    
                    // Mark as completed
                    booking.setStatus(BookingSession.BookingStatus.COMPLETED);
                    booking.setCoachNotes("Auto-completed by system after 24 hours");
                    bookingRepository.save(booking);
                    
                    // Transfer money from pending to available
                    coachBalanceService.completeBookingEarning(booking);
                    
                    completedCount++;
                } catch (Exception e) {
                    log.error("Error auto-completing booking {}", booking.getId(), e);
                }
            }
            
            if (completedCount > 0) {
                log.info("Auto-completed {} bookings", completedCount);
            }
            
        } catch (Exception e) {
            log.error("Error in autoCompleteBookings scheduled task", e);
        }
    }

    /**
     * Process scheduled payouts for coaches
     * Runs daily at midnight
     * 
     * Similar to:
     * - Grab: Weekly payout on Mondays
     * - Uber: Weekly payout
     * - Upwork: Payout after 10-day security period
     */
    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Transactional
    public void processScheduledPayouts() {
        try {
            // This is a placeholder for future payout automation
            // In real implementation, you would:
            // 1. Find all coaches with available balance > minimum threshold
            // 2. Create payout requests
            // 3. Integrate with banking API to transfer funds
            // 4. Update coach balance and create transaction records
            
            List<CoachBalance> balances = coachBalanceRepository.findAll();
            
            int pendingPayouts = 0;
            for (CoachBalance balance : balances) {
                if (balance.getAvailableBalance().doubleValue() >= 50000) { // Min 50k VND
                    pendingPayouts++;
                }
            }
            
            if (pendingPayouts > 0) {
                log.info("Found {} coaches with balance ready for payout", pendingPayouts);
                // TODO: Implement automatic payout or notify admin
            }
            
        } catch (Exception e) {
            log.error("Error in processScheduledPayouts scheduled task", e);
        }
    }

    /**
     * Clean up old pending bookings that were never confirmed
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    public void cleanupStaleBookings() {
        try {
            // Cancel bookings that are pending for more than 48 hours
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
            
            List<BookingSession> staleBookings = bookingRepository
                    .findByStatusAndCreatedAtBefore(
                            BookingSession.BookingStatus.PENDING,
                            cutoffTime.toInstant(java.time.ZoneOffset.UTC)
                    );
            
            int cancelledCount = 0;
            for (BookingSession booking : staleBookings) {
                try {
                    log.info("Auto-cancelling stale booking {}", booking.getId());
                    booking.setStatus(BookingSession.BookingStatus.CANCELLED);
                    booking.setCoachNotes("Auto-cancelled - pending too long without confirmation");
                    bookingRepository.save(booking);
                    cancelledCount++;
                } catch (Exception e) {
                    log.error("Error cancelling stale booking {}", booking.getId(), e);
                }
            }
            
            if (cancelledCount > 0) {
                log.info("Cancelled {} stale bookings", cancelledCount);
            }
            
        } catch (Exception e) {
            log.error("Error in cleanupStaleBookings scheduled task", e);
        }
    }
}
