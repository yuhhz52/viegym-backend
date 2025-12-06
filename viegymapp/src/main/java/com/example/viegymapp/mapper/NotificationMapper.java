package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.NotificationRequest;
import com.example.viegymapp.dto.response.NotificationResponse;
import com.example.viegymapp.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "emailSent", constant = "false")
    @Mapping(target = "pushSent", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    Notification toEntity(NotificationRequest request);
    
    NotificationResponse toResponse(Notification notification);
}
