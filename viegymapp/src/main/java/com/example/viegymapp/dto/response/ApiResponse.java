package com.example.viegymapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse <T>{
    @Builder.Default
    private int code = 1000;
    @Builder.Default
    private String message = "success";
    private T result;

}

