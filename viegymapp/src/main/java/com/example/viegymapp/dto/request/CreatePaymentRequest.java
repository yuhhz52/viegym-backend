package com.example.viegymapp.dto.request;

import com.example.viegymapp.entity.Enum.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    
    @NotNull(message = "Booking session ID is required")
    private UUID bookingSessionId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String description;
    
    private String returnUrl;
    
    private String notifyUrl;
}
