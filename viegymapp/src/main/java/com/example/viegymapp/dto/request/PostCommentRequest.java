package com.example.viegymapp.dto.request;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentRequest {
    private UUID parentCommentId;
    private String content;
}
