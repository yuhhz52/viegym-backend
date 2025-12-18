package com.example.viegymapp.service.Impl;

import com.example.viegymapp.entity.*;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.BookingSessionRepository;
import com.example.viegymapp.repository.CoachBalanceRepository;
import com.example.viegymapp.repository.CoachTransactionRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.CoachBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachBalanceServiceImpl implements CoachBalanceService {

    private final CoachBalanceRepository coachBalanceRepository;
    private final CoachTransactionRepository coachTransactionRepository;
    private final UserRepository userRepository;
    private final BookingSessionRepository bookingSessionRepository;
    
    @Value("${app.platform-fee-percentage:15.0}")
    private Double platformFeePercentage; // Default 15% platform fee
    
    /**
     * Get the most accurate current balance from last transaction
     */
    private BigDecimal getCurrentBalanceFromTransactions(UUID coachId) {
        // First try to get from latest transaction (most reliable)
        return coachTransactionRepository.findTopByCoachIdOrderByCreatedAtDescProcessedAtDesc(coachId)
                .map(CoachTransaction::getBalanceAfter)
                .orElseGet(() -> {
                    // Fallback: get from coach balance table
                    return coachBalanceRepository.findByCoachId(coachId)
                            .map(balance -> balance.getAvailableBalance().add(balance.getPendingBalance()))
                            .orElse(BigDecimal.ZERO);
                });
    }

    @Override
    @Transactional
    public void processBookingPayment(Payment payment) {
        User coach = payment.getBookingSession().getCoach();
        
        // Get or create coach balance
        CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseGet(() -> {
                    CoachBalance newBalance = CoachBalance.builder()
                            .coach(coach)
                            .availableBalance(BigDecimal.ZERO)
                            .pendingBalance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    return coachBalanceRepository.save(newBalance);
                });
        
        // Calculate platform fee and net amount
        BigDecimal amount = payment.getAmount();
        BigDecimal platformFee = amount.multiply(BigDecimal.valueOf(platformFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = amount.subtract(platformFee);
        
        // Record current balance for transaction history
        BigDecimal currentPendingBalance = balance.getPendingBalance();
        
        //  IMPORTANT: Only add to pending balance, don't make it available yet
        // Balance will be moved to available only when booking is COMPLETED
        balance.setPendingBalance(balance.getPendingBalance().add(netAmount));
        balance.setLastUpdated(LocalDateTime.now());
        coachBalanceRepository.save(balance);
        
        // Create transaction record with PENDING status
        CoachTransaction transaction = CoachTransaction.builder()
                .coach(coach)
                .payment(payment)
                .bookingSession(payment.getBookingSession())
                .type(CoachTransaction.TransactionType.EARNING)
                .amount(amount)
                .platformFee(platformFee)
                .netAmount(netAmount)
                .balanceBefore(currentPendingBalance)
                .balanceAfter(balance.getPendingBalance())
                .status(CoachTransaction.TransactionStatus.PENDING) // Keep as PENDING
                .description(String.format("Thanh toán từ booking #%s - Phí nền tảng %.0f%% (Đang chờ hoàn tất)", 
                        payment.getBookingSession().getId(), platformFeePercentage))
                .processedAt(LocalDateTime.now())
                .build();
        
        coachTransactionRepository.save(transaction);
        
        log.info("Added to pending balance for coach {}: Amount={}, Fee={}, Net={}, Status=PENDING", 
                coach.getId(), amount, platformFee, netAmount);
    }

    @Override
    @Transactional
    public void completeBookingEarning(BookingSession booking) {
        User coach = booking.getCoach();
        
        CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Find transactions for this booking session (PENDING or COMPLETED but money still in pendingBalance)
        // Note: Transaction might be COMPLETED (from confirmPaymentSuccess) but money still in pendingBalance
        List<CoachTransaction> transactions = coachTransactionRepository
                .findByBookingSessionId(booking.getId());
        
        if (transactions.isEmpty()) {
            log.warn("No earning transaction found for booking {}", booking.getId());
            return;
        }
        
        // Find transaction that hasn't been processed yet (money still in pendingBalance)
        // Check if there's money in pendingBalance that matches this booking
        BigDecimal totalNetAmount = BigDecimal.ZERO;
        CoachTransaction transactionToUpdate = null;
        
        for (CoachTransaction transaction : transactions) {
            if (transaction.getType() == CoachTransaction.TransactionType.EARNING) {
                BigDecimal netAmount = transaction.getNetAmount();
                
                // Check if this transaction's amount is still in pendingBalance
                // If pendingBalance >= netAmount, it means this transaction hasn't been moved to available yet
                if (balance.getPendingBalance().compareTo(netAmount) >= 0) {
                    totalNetAmount = netAmount;
                    transactionToUpdate = transaction;
                    break; // Usually just one transaction per booking
                }
            }
        }
        
        if (transactionToUpdate == null || totalNetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("No valid transaction to process for booking {} or money already moved. Pending balance: {}", 
                    booking.getId(), balance.getPendingBalance());
            return;
        }
        
        // Move from pending to available
        balance.setPendingBalance(balance.getPendingBalance().subtract(totalNetAmount));
        balance.setAvailableBalance(balance.getAvailableBalance().add(totalNetAmount));
        balance.setTotalEarned(balance.getTotalEarned().add(totalNetAmount));
        balance.setLastUpdated(LocalDateTime.now());
        coachBalanceRepository.save(balance);
        
        // Update transaction status and description
        transactionToUpdate.setStatus(CoachTransaction.TransactionStatus.COMPLETED);
        transactionToUpdate.setProcessedAt(LocalDateTime.now());
        transactionToUpdate.setDescription(
            transactionToUpdate.getDescription().replace("(Đang chờ hoàn tất)", "(Đã hoàn tất)")
        );
        coachTransactionRepository.save(transactionToUpdate);
        
        log.info("Completed earning for coach {}: Net amount={}, Booking={}", 
                coach.getId(), totalNetAmount, booking.getId());
    }

    @Override
    @Transactional
    public void processRefund(Payment payment, BigDecimal clientRefundAmount, String reason) {
        User coach = payment.getBookingSession().getCoach();
        
        log.info("=== REFUND PROCESS START ===");
        log.info("Coach ID: {}", coach.getId());
        log.info("Payment ID: {}", payment.getId());
        log.info("Client refund amount: {}", clientRefundAmount);
        
        // 🎯 CRITICAL FIX: Get current balance from LAST TRANSACTION (most reliable)
        BigDecimal currentTotalBalance = getCurrentBalanceFromTransactions(coach.getId());
        log.info("Current balance from transactions: {}", currentTotalBalance);
        
        // Verify with coach balance entity
        CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        BigDecimal entityBalance = balance.getAvailableBalance().add(balance.getPendingBalance());
        log.info("Balance from entity: Available={}, Pending={}, Total={}", 
                balance.getAvailableBalance(), balance.getPendingBalance(), entityBalance);
        
        if (!currentTotalBalance.equals(entityBalance)) {
            log.warn("⚠️ Balance MISMATCH detected! Transaction: {}, Entity: {}, Diff: {}", 
                    currentTotalBalance, entityBalance, currentTotalBalance.subtract(entityBalance));
            
            // Sync entity balance with transaction balance (transaction is source of truth)
            BigDecimal diff = currentTotalBalance.subtract(entityBalance);
            balance.setAvailableBalance(balance.getAvailableBalance().add(diff));
            log.info("🔧 Syncing entity balance: {} + {} = {}", 
                    entityBalance, diff, currentTotalBalance);
        }
        
        // Calculate what coach originally received (net amount after platform fee)
        BigDecimal originalAmount = payment.getAmount();
        BigDecimal originalPlatformFee = originalAmount.multiply(BigDecimal.valueOf(platformFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal originalNetAmount = originalAmount.subtract(originalPlatformFee);
        
        // Calculate refund percentage based on client refund amount
        BigDecimal refundPercentage = clientRefundAmount.divide(originalAmount, 4, RoundingMode.HALF_UP);
        
        // Coach needs to refund percentage of what they originally received
        BigDecimal coachRefundAmount = originalNetAmount.multiply(refundPercentage)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Platform refunds percentage of fee they collected
        BigDecimal platformFeeRefund = originalPlatformFee.multiply(refundPercentage)
                .setScale(2, RoundingMode.HALF_UP);
        
        log.info("Refund calculation: {}% of {} = {}", 
                refundPercentage.multiply(BigDecimal.valueOf(100)), originalNetAmount, coachRefundAmount);
        
        // Record balances for transaction history (BEFORE any changes)
        BigDecimal balanceBeforeRefund = currentTotalBalance; // 🎯 FROM TRANSACTIONS - MOST ACCURATE
        
        // First, try to deduct from pending balance (if payment was not yet completed)
        if (balance.getPendingBalance().compareTo(coachRefundAmount) >= 0) {
            balance.setPendingBalance(balance.getPendingBalance().subtract(coachRefundAmount));
            log.info("Deducted {} from pending balance", coachRefundAmount);
        } else {
            // Deduct remaining from pending first
            BigDecimal remainingFromPending = balance.getPendingBalance();
            balance.setPendingBalance(BigDecimal.ZERO);
            
            // Then deduct the rest from available balance
            BigDecimal remainingToDeduct = coachRefundAmount.subtract(remainingFromPending);
            balance.setAvailableBalance(balance.getAvailableBalance().subtract(remainingToDeduct));
            
            log.info("Deducted {} from pending, {} from available", 
                    remainingFromPending, remainingToDeduct);
        }
        
        // Calculate new total balance AFTER changes
        BigDecimal newTotalBalance = balance.getAvailableBalance().add(balance.getPendingBalance());
        
        log.info("Balance flow: {} → {} (difference: {})", 
                balanceBeforeRefund, newTotalBalance, newTotalBalance.subtract(balanceBeforeRefund));
        
        balance.setLastUpdated(LocalDateTime.now());
        coachBalanceRepository.saveAndFlush(balance); // 🎯 FORCE IMMEDIATE SAVE
        
        // Create refund transaction with CORRECT balance values
        String refundPercentageStr = refundPercentage.multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP) + "%";
        
        CoachTransaction transaction = CoachTransaction.builder()
                .coach(coach)
                .payment(payment)
                .bookingSession(payment.getBookingSession())
                .type(CoachTransaction.TransactionType.REFUND)
                .amount(originalAmount)
                .platformFee(platformFeeRefund) // Platform refunds proportional fee
                .netAmount(coachRefundAmount.negate()) // Negative because it's a deduction
                .balanceBefore(balanceBeforeRefund) // 🎯 FROM TRANSACTIONS - GUARANTEED ACCURATE
                .balanceAfter(newTotalBalance) // 🎯 CALCULATED AFTER REFUND
                .status(CoachTransaction.TransactionStatus.COMPLETED)
                .description(String.format("Hoàn %s - %s", refundPercentageStr, reason))
                .processedAt(LocalDateTime.now())
                .build();
        
        coachTransactionRepository.saveAndFlush(transaction); // 🎯 FORCE IMMEDIATE SAVE
        
        // VALIDATION: Check balance consistency
        BigDecimal expectedBalance = balanceBeforeRefund.add(coachRefundAmount.negate());
        if (!newTotalBalance.equals(expectedBalance)) {
            log.error(" Balance calculation ERROR! Expected: {}, Actual: {}",
                    expectedBalance, newTotalBalance);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        
        log.info("Processed refund for coach {}: Client refund={} ({}), Coach refund={}, Platform fee refund={}",
                coach.getId(), clientRefundAmount, refundPercentageStr, coachRefundAmount, platformFeeRefund);
        log.info("Final balance: Available={}, Pending={}, Total={}",
                balance.getAvailableBalance(), balance.getPendingBalance(), newTotalBalance);
        log.info("=== REFUND PROCESS END ===");
    }

    @Override
    @Transactional
    public void cancelPendingPayment(Payment payment, String reason) {
        User coach = payment.getBookingSession().getCoach();
        
        CoachBalance balance = coachBalanceRepository.findByCoachId(coach.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Find the pending transaction for this payment
        CoachTransaction pendingTransaction = coachTransactionRepository.findByPaymentId(payment.getId())
                .stream()
                .filter(t -> t.getStatus() == CoachTransaction.TransactionStatus.PENDING)
                .findFirst()
                .orElse(null);
        
        if (pendingTransaction != null) {
            BigDecimal netAmount = pendingTransaction.getNetAmount();
            
            // Remove from pending balance (reverse the addition)
            balance.setPendingBalance(balance.getPendingBalance().subtract(netAmount));
            balance.setLastUpdated(LocalDateTime.now());
            coachBalanceRepository.save(balance);
            
            // Mark transaction as cancelled
            pendingTransaction.setStatus(CoachTransaction.TransactionStatus.CANCELLED);
            pendingTransaction.setDescription(pendingTransaction.getDescription() + " - HỦY: " + reason);
            pendingTransaction.setProcessedAt(LocalDateTime.now());
            coachTransactionRepository.save(pendingTransaction);
            
            log.info("Cancelled pending payment for coach {}: Removed {} from pending balance", 
                    coach.getId(), netAmount);
        }
    }
    
    @Override
    @Transactional
    public void confirmPaymentSuccess(Payment payment) {
        User coach = payment.getBookingSession().getCoach();
        
        // Find the pending transaction for this payment
        CoachTransaction pendingTransaction = coachTransactionRepository.findByPaymentId(payment.getId())
                .stream()
                .filter(t -> t.getStatus() == CoachTransaction.TransactionStatus.PENDING)
                .findFirst()
                .orElse(null);
        
        if (pendingTransaction != null) {
            // Update transaction status to COMPLETED
            pendingTransaction.setStatus(CoachTransaction.TransactionStatus.COMPLETED);
            pendingTransaction.setDescription(pendingTransaction.getDescription().replace("(Đang chờ hoàn tất)", "(Đã hoàn tất)"));
            pendingTransaction.setProcessedAt(LocalDateTime.now());
            coachTransactionRepository.save(pendingTransaction);
            
            log.info("Confirmed payment success for coach {}: Transaction {} marked as COMPLETED", 
                    coach.getId(), pendingTransaction.getId());
        } else {
            log.warn("No pending transaction found for payment {} and coach {}", payment.getId(), coach.getId());
        }
    }

    @Override
    public BigDecimal getCoachAvailableBalance(UUID coachId) {
        return coachBalanceRepository.findByCoachId(coachId)
                .map(CoachBalance::getAvailableBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getCoachPendingBalance(UUID coachId) {
        return coachBalanceRepository.findByCoachId(coachId)
                .map(CoachBalance::getPendingBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public int processPendingCompletedBookings(UUID coachId) {
        // Find all completed bookings for this coach
        List<BookingSession> completedBookings = bookingSessionRepository
                .findCompletedBookingsByCoachId(coachId);
        
        int processedCount = 0;
        for (BookingSession booking : completedBookings) {
            // Check if this booking has pending transactions
            List<CoachTransaction> pendingTransactions = coachTransactionRepository
                    .findPendingEarningsByBookingSessionId(booking.getId());
            
            if (!pendingTransactions.isEmpty()) {
                try {
                    // Process this booking
                    completeBookingEarning(booking);
                    processedCount++;
                    log.info("Processed pending transaction for completed booking {} (coach {})", 
                            booking.getId(), coachId);
                } catch (Exception e) {
                    log.error("Error processing booking {} for coach {}: {}", 
                            booking.getId(), coachId, e.getMessage(), e);
                }
            }
        }
        
        log.info("Processed {} completed bookings with pending transactions for coach {}", 
                processedCount, coachId);
        return processedCount;
    }

    @Override
    @Transactional
    public void initializeCoachBalance(UUID coachId) {
        if (!coachBalanceRepository.existsByCoachId(coachId)) {
            User coach = userRepository.findById(coachId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            CoachBalance balance = CoachBalance.builder()
                    .coach(coach)
                    .availableBalance(BigDecimal.ZERO)
                    .pendingBalance(BigDecimal.ZERO)
                    .totalEarned(BigDecimal.ZERO)
                    .totalWithdrawn(BigDecimal.ZERO)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            
            coachBalanceRepository.save(balance);
            log.info("Initialized balance for coach {}", coachId);
        }
    }
}
