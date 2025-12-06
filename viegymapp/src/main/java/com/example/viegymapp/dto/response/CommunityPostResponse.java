package com.example.viegymapp.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostResponse {
    private UUID id;
    private String title;
    private String content;
    private String status;
    private Instant createdAt;
    private String authorName;
    private String authorAvatar;
    private List<String> mediaUrls;
    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;
    private Integer reportCount;
    private Boolean isReportedByCurrentUser;
}
