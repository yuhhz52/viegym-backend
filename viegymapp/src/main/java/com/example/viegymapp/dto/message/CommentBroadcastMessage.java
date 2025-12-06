package com.example.viegymapp.dto.message;

import com.example.viegymapp.dto.response.PostCommentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentBroadcastMessage {
    private UUID postId;
    private PostCommentResponse comment;
}

