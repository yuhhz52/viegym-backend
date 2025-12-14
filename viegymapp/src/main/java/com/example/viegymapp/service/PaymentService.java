package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.CreatePaymentRequest;
import com.example.viegymapp.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    
    /**
     * Tạo payment URL cho VNPay
     */
    PaymentResponse createVNPayPayment(CreatePaymentRequest request, UUID userId, String ipAddress);
    
    /**
     * Xử lý callback từ VNPay
     */
    PaymentResponse handleVNPayCallback(Map<String, String> params);
    
    /**
     * Kiểm tra trạng thái payment
     */
    PaymentResponse checkPaymentStatus(String orderId);
    
    /**
     * Lấy payment theo booking session
     */
    PaymentResponse getPaymentByBookingSession(UUID bookingSessionId);
    
    /**
     * Lấy danh sách payment của user
     */
    List<PaymentResponse> getUserPayments(UUID userId);
    
    /**
     * Hoàn tiền
     */
    PaymentResponse refundPayment(UUID paymentId, String reason);
}
