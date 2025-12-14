// Backend endpoint cần tạo:

// DTO for slot payment request
public class CreateSlotPaymentRequest {
    private UUID timeSlotId;
    private UUID coachId;
    private String clientNotes;
    private BigDecimal amount;
    private String paymentMethod;
    private String description;
    private String returnUrl;
    private String notifyUrl;
    
    // getters and setters...
}

// Trong PaymentController, thêm endpoint:
@PostMapping("/vnpay/create-slot-payment")
public ResponseEntity<ApiResponse<PaymentResponse>> createSlotPayment(
    @RequestBody CreateSlotPaymentRequest request
) {
    PaymentResponse response = paymentService.createSlotPayment(request);
    return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
        .code(200)
        .message("Payment created successfully")
        .result(response)
        .build());
}

// Trong PaymentService, implement:
public PaymentResponse createSlotPayment(CreateSlotPaymentRequest request) {
    // 1. Validate slot exists and is available
    // 2. Create temporary payment record with slot info
    // 3. Generate VNPay payment URL
    // 4. Return PaymentResponse with payUrl
    // 5. When payment callback is successful, create booking with CONFIRMED status
}