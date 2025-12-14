package com.example.viegymapp.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSlotPaymentRequest {
    UUID timeSlotId;
    UUID coachId;
    String clientNotes;
    BigDecimal amount;
    String paymentMethod;
    String description;
    String returnUrl;
    String notifyUrl;
}