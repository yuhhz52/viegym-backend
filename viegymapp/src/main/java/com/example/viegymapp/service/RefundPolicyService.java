package com.example.viegymapp.service;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service to handle refund policies based on cancellation timing
 * Similar to Grab, Uber, Airbnb refund policies
 */
public interface RefundPolicyService {
    
    /**
     * Calculate refund amount based on when booking is cancelled
     * 
     * Refund Policy (similar to real-world platforms):
     * - Cancel > 24 hours before: 100% refund
     * - Cancel 12-24 hours before: 75% refund
     * - Cancel 2-12 hours before: 50% refund
     * - Cancel < 2 hours before: 25% refund
     * - No-show or late cancel: 0% refund
     * 
     * @param booking The booking being cancelled
     * @param payment The payment for the booking
     * @return The amount that should be refunded
     */
    BigDecimal calculateRefundAmount(BookingSession booking, Payment payment);
    
    /**
     * Get refund percentage based on hours before booking
     */
    int getRefundPercentage(long hoursBeforeBooking);
    
    /**
     * Check if refund is allowed
     */
    boolean isRefundAllowed(BookingSession booking);
    
    /**
     * Get refund policy description for UI
     */
    String getRefundPolicyDescription(LocalDateTime bookingTime);
}
