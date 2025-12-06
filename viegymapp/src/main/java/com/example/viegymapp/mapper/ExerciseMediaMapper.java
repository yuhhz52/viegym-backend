package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.ExerciseMediaRequest;
import com.example.viegymapp.dto.response.ExerciseMediaResponse;
import com.example.viegymapp.entity.ExerciseMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExerciseMediaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    ExerciseMedia toEntity(ExerciseMediaRequest dto);

    ExerciseMediaResponse toResponseDTO(ExerciseMedia entity);
}

