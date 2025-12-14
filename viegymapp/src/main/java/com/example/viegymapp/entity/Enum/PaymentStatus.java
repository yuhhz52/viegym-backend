package com.example.viegymapp.entity.Enum;

public enum PaymentStatus {
    PENDING,      // Chờ thanh toán
    PROCESSING,   // Đang xử lý
    COMPLETED,    // Thanh toán thành công
    FAILED,       // Thanh toán thất bại
    REFUNDED,     // Đã hoàn tiền
    CANCELLED     // Đã hủy
}
