package com.example.viegymapp.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportResponse {
    private UUID id;
    private UUID postId;
    private String postTitle;
    private UUID reporterId;
    private String reporterName;
    private String reason;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
}
