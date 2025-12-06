package com.example.viegymapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {
    private Boolean darkMode;
    private Boolean notifications;
    private String language;
}