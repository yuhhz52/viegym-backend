package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.CreatePaymentRequest;
import com.example.viegymapp.dto.response.PaymentResponse;
import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.Payment;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.CoachTimeSlot;
import com.example.viegymapp.entity.Enum.PaymentStatus;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.BookingSessionRepository;
import com.example.viegymapp.repository.PaymentRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.repository.CoachTimeSlotRepository;
import com.example.viegymapp.service.PaymentService;
import com.example.viegymapp.service.CoachBalanceService;
import com.example.viegymapp.service.RefundPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingSessionRepository bookingSessionRepository;
    private final UserRepository userRepository;
    private final CoachTimeSlotRepository coachTimeSlotRepository;
    private final ObjectMapper objectMapper;
    private final CoachBalanceService coachBalanceService;
    private final RefundPolicyService refundPolicyService;

    @Value("${vnpay.tmn-code}")
    private String vnpayTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpayHashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String vnpayReturnUrl;

    @Override
    public PaymentResponse checkPaymentStatus(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByBookingSession(UUID bookingSessionId) {
        Payment payment = paymentRepository.findByBookingSessionId(bookingSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return mapToPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getUserPayments(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new AppException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        // Calculate refund amount using RefundPolicyService
        BookingSession booking = payment.getBookingSession();
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(booking, payment);
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);
        
        try {
            coachBalanceService.processRefund(payment, refundAmount, reason);
        } catch (Exception e) {
            log.error("Error processing refund for coach balance, payment {}", payment.getId(), e);
        }

        return mapToPaymentResponse(payment);
    }

    private String hmacSHA512(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Override
    @Transactional
    public PaymentResponse createVNPayPayment(CreatePaymentRequest request, UUID userId, String ipAddress) {
        log.info("=== CREATE VNPAY PAYMENT ===");
        
        BookingSession bookingSession = bookingSessionRepository.findById(request.getBookingSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!bookingSession.getClient().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Validate booking is PENDING (not expired or already confirmed)
        if (bookingSession.getStatus() != BookingSession.BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
        
        // Check if booking has expired
        if (bookingSession.getExpiredAt() != null && bookingSession.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String orderId = "VIEGYM_" + System.currentTimeMillis();
        
        Payment payment = Payment.builder()
                .bookingSession(bookingSession)
                .user(user)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .orderId(orderId)
                .description(request.getDescription())
                .build();

        payment = paymentRepository.save(payment);

        try {
            String orderInfo = "Thanh toan dat lich tap voi coach " + bookingSession.getCoach().getFullName();
            orderInfo = orderInfo.replaceAll("[^a-zA-Z0-9 ]", "");
            
            String paymentUrl = generateVNPayUrl(payment.getAmount(), orderId, orderInfo, ipAddress);
            
            payment.setPaymentResponse(paymentUrl);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .id(payment.getId())
                    .bookingSessionId(payment.getBookingSession().getId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .orderId(payment.getOrderId())
                    .description(payment.getDescription())
                    .createdAt(payment.getCreatedAt())
                    .payUrl(paymentUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new AppException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    @Transactional
    public PaymentResponse handleVNPayCallback(Map<String, String> params) {
        log.info("========== VNPay Callback Verification ==========");
        log.info("All callback params: {}", params);

        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        try {
            // Verify signature
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder data = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = params.get(fieldName);
                
                String encodedKey = java.net.URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                
                if (i > 0) {
                    data.append('&');
                }
                data.append(encodedKey).append('=').append(encodedValue);
            }
            
            String calculatedHash = hmacSHA512(data.toString(), vnpayHashSecret);

            log.info("Calculated hash: {}, Received hash: {}, Match: {}", 
                    calculatedHash, vnpSecureHash, calculatedHash.equals(vnpSecureHash));

            if (!calculatedHash.equals(vnpSecureHash)) {
                log.error("Invalid VNPay signature");
                throw new AppException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
            }

            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionId = params.get("vnp_TransactionNo");

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

            if ("00".equals(responseCode)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(transactionId);
                payment.setPaidAt(LocalDateTime.now());
                
                // Update booking status to CONFIRMED
                BookingSession booking = payment.getBookingSession();
                if (booking != null) {
                    // Check if booking is still PENDING (not expired)
                    if (booking.getStatus() == BookingSession.BookingStatus.PENDING) {
                        booking.setStatus(BookingSession.BookingStatus.CONFIRMED);
                        booking.setRequiresPayment(false);
                        bookingSessionRepository.save(booking);
                        
                        // Update slot booked_count
                        CoachTimeSlot slot = booking.getTimeSlot();
                        if (slot != null) {
                            slot.setBookedCount(slot.getBookedCount() + 1);
                            
                            // If slot is full, update status to FULL
                            if (slot.getBookedCount() >= slot.getCapacity()) {
                                slot.setStatus(CoachTimeSlot.SlotStatus.FULL);
                            }
                            coachTimeSlotRepository.save(slot);
                            
                            log.info("Payment successful for booking {}. Slot {} booked_count updated to {}/{}", 
                                    booking.getId(), slot.getId(), slot.getBookedCount(), slot.getCapacity());
                        }
                    } else {
                        log.warn("Payment succeeded but booking {} is not PENDING (status: {}). Skipping confirmation.", 
                                booking.getId(), booking.getStatus());
                    }
                } else {
                    log.warn("Payment {} succeeded but has no associated booking", payment.getId());
                }
                
                // Process coach balance - add to pending first
                try {
                    coachBalanceService.processBookingPayment(payment);
                    // Then confirm payment success to update transaction status
                    coachBalanceService.confirmPaymentSuccess(payment);
                } catch (Exception e) {
                    log.error("Error processing coach balance for payment {}", payment.getId(), e);
                }
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("VNPay response code: " + responseCode);
                
                // Cancel any pending payment in coach balance (if it was added)
                try {
                    coachBalanceService.cancelPendingPayment(payment, "Payment failed: " + responseCode);
                } catch (Exception e) {
                    log.error("Error cancelling pending payment for coach balance, payment {}", payment.getId(), e);
                }
                
                // Payment failed - booking remains PENDING and will expire after 10 minutes
                // No need to update slot as booking was never confirmed
                log.info("Payment failed for booking {}. Booking remains PENDING and will expire.", 
                        payment.getBookingSession() != null ? payment.getBookingSession().getId() : "unknown");
            }

            payment = paymentRepository.save(payment);
            return mapToPaymentResponse(payment);
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error handling VNPay callback", e);
            throw new AppException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
    }


    /**
     * Generate VNPay payment URL - extracted common logic
     */
    private String generateVNPayUrl(java.math.BigDecimal amount, String orderId, String orderInfo, String ipAddress) throws Exception {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount.longValue() * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");
        
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnpCreateDate = formatter.format(cal.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cal.add(java.util.Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cal.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        log.info("========== VNPay Payment URL Generation ==========");
        log.info("Params: {}", vnpParams);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            
            String encodedKey = java.net.URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
            String encodedValue = java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
            
            if (i > 0) {
                hashData.append('&');
                query.append('&');
            }
            hashData.append(encodedKey).append('=').append(encodedValue);
            query.append(encodedKey).append('=').append(encodedValue);
        }

        String vnpSecureHash = hmacSHA512(hashData.toString(), vnpayHashSecret);
        String paymentUrl = vnpayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
        
        log.info("Generated payment URL: {}", paymentUrl);
        log.info("=========================================");

        return paymentUrl;
    }


    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingSessionId(payment.getBookingSession() != null ? payment.getBookingSession().getId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

}