package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.CreatePaymentRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.PaymentResponse;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse<PaymentResponse>> createVNPayPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        String ipAddress = httpRequest.getRemoteAddr();
        PaymentResponse response = paymentService.createVNPayPayment(request, user.getId(), ipAddress);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Tạo thanh toán thành công")
                .result(response)
                .build());
    }


    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> handleVNPayCallback(@RequestParam Map<String, String> params) {
        log.info("VNPay callback received: {}", params);
        
        try {
            PaymentResponse response = paymentService.handleVNPayCallback(params);
            
            String redirectUrl = String.format(
                "http://localhost:5173/payment/callback?orderId=%s&status=%s",
                response.getOrderId(),
                response.getStatus()
            );
            
            return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("Error handling VNPay callback", e);
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:5173/payment/callback?status=FAILED")
                    .build();
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> checkPaymentStatus(
            @PathVariable String orderId) {
        
        PaymentResponse response = paymentService.checkPaymentStatus(orderId);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Lấy trạng thái thanh toán thành công")
                .result(response)
                .build());
    }

    @GetMapping("/booking/{bookingSessionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(
            @PathVariable UUID bookingSessionId) {
        
        PaymentResponse response = paymentService.getPaymentByBookingSession(bookingSessionId);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Lấy thông tin thanh toán thành công")
                .result(response)
                .build());
    }

    @GetMapping("/my-payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        List<PaymentResponse> responses = paymentService.getUserPayments(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .code(200)
                .message("Lấy danh sách thanh toán thành công")
                .result(responses)
                .build());
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam(required = false) String reason) {
        
        PaymentResponse response = paymentService.refundPayment(paymentId, reason);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Hoàn tiền thành công")
                .result(response)
                .build());
    }
}
