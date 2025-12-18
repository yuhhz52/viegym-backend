package com.example.viegymapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret
    ) {
        // Only initialize Cloudinary if credentials are provided
        if (cloudName != null && !cloudName.isEmpty() && 
            apiKey != null && !apiKey.isEmpty() && 
            apiSecret != null && !apiSecret.isEmpty()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
            log.info("Cloudinary service initialized successfully");
        } else {
            this.cloudinary = null;
            log.warn("Cloudinary credentials not provided. File upload functionality will be disabled.");
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (cloudinary == null) {
            log.error("Cloudinary is not configured. Cannot upload file.");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "viegym"
                    )
            );
            
            String secureUrl = uploadResult.get("secure_url").toString();
            log.info("File uploaded successfully to Cloudinary: {}", secureUrl);
            return secureUrl;
            
        } catch (IOException e) {
            log.error("IO error while uploading file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Unexpected error while uploading file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
