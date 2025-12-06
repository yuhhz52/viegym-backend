package com.example.viegymapp.controller;

import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final CloudinaryService cloudinaryService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/mpeg", "video/quicktime"};

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // Validate file không null hoặc empty
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || 
            (!isAllowedImageType(contentType) && !isAllowedVideoType(contentType))) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        String url = cloudinaryService.uploadFile(file);
        return ApiResponse.<String>builder()
                .result(url)
                .build();
    }

    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedVideoType(String contentType) {
        for (String allowedType : ALLOWED_VIDEO_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
