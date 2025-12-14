package com.example.viegymapp.service;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.Payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for managing coach balances and earnings
 */
public interface CoachBalanceService {
    
    /**
     * Process payment for a completed booking
     * This will add money to coach's pending balance
     */
    void processBookingPayment(Payment payment);
    
    /**
     * Complete booking and transfer money from pending to available
     */
    void completeBookingEarning(BookingSession booking);
    
    /**
     * Process refund - deduct money from coach's balance
     */
    void processRefund(Payment payment, BigDecimal refundAmount, String reason);
    
    /**
     * Cancel a pending payment transaction (when payment fails or booking cancelled before completion)
     */
    void cancelPendingPayment(Payment payment, String reason);
    
    /**
     * Confirm payment success - update transaction status from PENDING to COMPLETED
     */
    void confirmPaymentSuccess(Payment payment);
    
    /**
     * Get coach's current balance
     */
    BigDecimal getCoachAvailableBalance(UUID coachId);
    
    /**
     * Get coach's pending balance
     */
    BigDecimal getCoachPendingBalance(UUID coachId);
    
    /**
     * Initialize balance for a new coach
     */
    void initializeCoachBalance(UUID coachId);
}
