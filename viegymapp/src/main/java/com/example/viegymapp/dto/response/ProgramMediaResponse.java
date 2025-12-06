package com.example.viegymapp.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramMediaResponse {
    private UUID id;
    private String mediaType;
    private String url;
    private String caption;
    private Integer orderNo;
}
