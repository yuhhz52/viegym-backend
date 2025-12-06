package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.SessionExerciseLogRequest;
import com.example.viegymapp.dto.response.SessionExerciseLogResponse;
import com.example.viegymapp.entity.SessionExerciseLog;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface SessionExerciseLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    SessionExerciseLog toEntity(SessionExerciseLogRequest request);

    @Mapping(source = "session.id", target = "sessionId")
    @Mapping(source = "exercise.id", target = "exerciseId")
    @Mapping(target = "volume", expression = "java(entity.calculateVolume())")
    @Mapping(target = "displayValue", expression = "java(entity.getDisplayValue())")
    SessionExerciseLogResponse toResponse(SessionExerciseLog entity);
}
