package com.example.viegymapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagingResponse<T> {
    private List<T> data;
    private long total;
    private int page;
    private int size;
}
