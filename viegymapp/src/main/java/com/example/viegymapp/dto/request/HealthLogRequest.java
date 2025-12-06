package com.example.viegymapp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthLogRequest {
    @Schema(description = "Thời điểm ghi nhận", example = "2025-10-07T12:00:00Z")
    private Instant recordedAt;

    @Schema(description = "Cân nặng (kg)", example = "70.5")
    private Double weightKg;

    @Schema(description = "Tỉ lệ mỡ (%)", example = "15.2")
    private Double bodyFatPercent;

    @Schema(description = "Khối lượng cơ (kg)", example = "55.3")
    private Double muscleMassKg;

    @Schema(description = "Vòng eo (cm)", example = "80.5")
    private Double waistCm;

    @Schema(description = "Nhịp tim", example = "72")
    private Integer heartRate;

    @Schema(description = "Ghi chú", example = "Sau khi tập gym buổi sáng")
    private String note;
}
