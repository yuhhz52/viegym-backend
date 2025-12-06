// mapper/HealthLogMapper.java
package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.HealthLogRequest;
import com.example.viegymapp.dto.response.HealthLogResponse;
import com.example.viegymapp.entity.HealthLog;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HealthLogMapper {
    HealthLog toEntity(HealthLogRequest request);
    HealthLogResponse toResponse(HealthLog entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget HealthLog entity, HealthLogRequest request);
}
