package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.NotificationPreferenceRequest;
import com.example.viegymapp.dto.response.NotificationPreferenceResponse;
import com.example.viegymapp.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationPreferenceMapper {
    
    NotificationPreferenceResponse toResponse(NotificationPreference preference);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromRequest(NotificationPreferenceRequest request, @MappingTarget NotificationPreference preference);
}
