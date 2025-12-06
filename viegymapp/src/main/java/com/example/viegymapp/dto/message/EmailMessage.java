package com.example.viegymapp.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private String toEmail;
    private String subject;
    private String resetToken;
    private EmailType emailType;
    
    public enum EmailType {
        PASSWORD_RESET,
        WELCOME,
        NOTIFICATION
    }
}
