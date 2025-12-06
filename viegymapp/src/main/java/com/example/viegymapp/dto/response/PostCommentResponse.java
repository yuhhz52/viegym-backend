package com.example.viegymapp.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentResponse {
    private UUID id;
    private String content;
    private String authorName;
    private String authorAvatar;
    private Instant createdAt;
    private UUID parentCommentId;
    @Builder.Default
    private List<PostCommentResponse> replies = List.of();
}
