package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.WorkoutSessionRequest;
import com.example.viegymapp.dto.response.WorkoutSessionResponse;
import com.example.viegymapp.entity.WorkoutSession;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkoutSessionMapper {

    // Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "logs", ignore = true)
    WorkoutSession toEntity(WorkoutSessionRequest request);

    // Entity -> Response
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "program.id", target = "programId")
    WorkoutSessionResponse toResponse(WorkoutSession entity);
}
