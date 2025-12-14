package com.example.viegymapp.dto.response;

import com.example.viegymapp.entity.Enum.PaymentMethod;
import com.example.viegymapp.entity.Enum.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    private UUID id;
    private UUID bookingSessionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String orderId;
    private String transactionId;
    private String description;
    private LocalDateTime paidAt;
    private OffsetDateTime createdAt;
    
    // For payment URL response
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
}
