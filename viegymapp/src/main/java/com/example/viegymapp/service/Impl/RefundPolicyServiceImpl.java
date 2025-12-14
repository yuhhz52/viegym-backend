package com.example.viegymapp.service.Impl;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.Payment;
import com.example.viegymapp.service.RefundPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
@Service
@Slf4j
public class RefundPolicyServiceImpl implements RefundPolicyService {

    @Override
    public BigDecimal calculateRefundAmount(BookingSession booking, Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingTime = booking.getBookingTime();
        
        log.info("=== REFUND CALCULATION DEBUG ===");
        log.info("Current time: {}", now);
        log.info("Booking time: {}", bookingTime);
        
        // Check if booking already passed (no-show or late cancellation)
        if (now.isAfter(bookingTime)) {
            log.info("Booking {} already passed. No refund.", booking.getId());
            return BigDecimal.ZERO;
        }
        
        // Calculate hours until booking
        long hoursUntilBooking = Duration.between(now, bookingTime).toHours();
        long minutesUntilBooking = Duration.between(now, bookingTime).toMinutes();
        
        log.info("Time until booking: {} hours ({} minutes)", hoursUntilBooking, minutesUntilBooking);
        
        // Get refund percentage based on cancellation timing
        int refundPercentage = getRefundPercentage(hoursUntilBooking);
        
        log.info("Refund percentage for {} hours before: {}%", hoursUntilBooking, refundPercentage);
        
        // Calculate refund amount
        BigDecimal refundAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(refundPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.info("Booking {} cancellation: {} hours before, {}% refund = {}", 
                booking.getId(), hoursUntilBooking, refundPercentage, refundAmount);
        log.info("=== END REFUND CALCULATION ===");
        
        return refundAmount;
    }

    @Override
    public int getRefundPercentage(long hoursBeforeBooking) {
        if (hoursBeforeBooking >= 24) {
            return 100; // Full refund if cancelled 24+ hours before
        } else if (hoursBeforeBooking >= 12) {
            return 75;  // 75% refund if cancelled 12-24 hours before
        } else if (hoursBeforeBooking >= 2) {
            return 50;  // 50% refund if cancelled 2-12 hours before
        } else if (hoursBeforeBooking >= 1) {
            return 25;  // 25% refund if cancelled 1-2 hours before
        } else {
            return 0;   // No refund if cancelled < 1 hour before or after booking time
        }
    }

    @Override
    public boolean isRefundAllowed(BookingSession booking) {
        // Can always request cancellation, but refund amount may be 0
        return booking.getStatus() == BookingSession.BookingStatus.PENDING 
            || booking.getStatus() == BookingSession.BookingStatus.CONFIRMED;
    }

    @Override
    public String getRefundPolicyDescription(LocalDateTime bookingTime) {
        LocalDateTime now = LocalDateTime.now();
        
        if (bookingTime == null) {
            return "Chính sách hoàn tiền áp dụng theo thời gian hủy lịch";
        }
        
        long hoursUntil = Duration.between(now, bookingTime).toHours();
        int percentage = getRefundPercentage(hoursUntil);
        
        if (percentage == 100) {
            return "Hủy miễn phí - Hoàn lại 100%";
        } else if (percentage >= 75) {
            return "Hoàn lại 75% nếu hủy ngay";
        } else if (percentage >= 50) {
            return "Hoàn lại 50% nếu hủy ngay";
        } else if (percentage >= 25) {
            return "Hoàn lại 25% nếu hủy ngay";
        } else {
            return "Không hoàn tiền nếu hủy ngay";
        }
    }
}
