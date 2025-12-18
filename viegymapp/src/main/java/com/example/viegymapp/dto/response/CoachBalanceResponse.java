package com.example.viegymapp.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachBalanceResponse {
    private BigDecimal availableBalance; // Số tiền có thể rút
    private BigDecimal pendingBalance; // Số tiền đang chờ (từ booking chưa hoàn thành)
    private BigDecimal totalEarned; // Tổng số tiền đã kiếm được
    private BigDecimal totalWithdrawn; // Tổng số tiền đã rút
    private String bankAccountInfo; // Thông tin tài khoản ngân hàng
    private LocalDateTime lastUpdated;
}

