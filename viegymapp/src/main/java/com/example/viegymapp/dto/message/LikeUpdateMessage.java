package com.example.viegymapp.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeUpdateMessage {
    // Use String instead of UUID for better JSON serialization
    private String postId;
    private Long likeCount;
}
