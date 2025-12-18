package com.example.viegymapp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawRequest {
    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "50000", message = "Số tiền rút tối thiểu là 50,000 VNĐ")
    private BigDecimal amount;
    
    @NotBlank(message = "Thông tin tài khoản ngân hàng không được để trống")
    private String bankAccountInfo; // Số tài khoản, tên ngân hàng, tên chủ tài khoản
}

