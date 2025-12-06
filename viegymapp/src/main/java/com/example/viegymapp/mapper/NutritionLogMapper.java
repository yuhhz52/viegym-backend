package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.NutritionLogRequest;
import com.example.viegymapp.dto.response.NutritionLogResponse;
import com.example.viegymapp.entity.NutritionLog;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutritionLogMapper {

    // Map từ request -> entity (khi tạo mới)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // sẽ set thủ công trong service
    NutritionLog toEntity(NutritionLogRequest request);

    // Map từ entity -> response
    @Mapping(source = "user.id", target = "userId")
    NutritionLogResponse toResponse(NutritionLog entity);

    List<NutritionLogResponse> toResponseList(List<NutritionLog> entities);

    // Cập nhật entity từ request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(NutritionLogRequest request, @MappingTarget NutritionLog entity);
}
