package com.example.viegymapp.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostRequest {
    private String title;
    private String content;
    private List<String> mediaUrls;
}
