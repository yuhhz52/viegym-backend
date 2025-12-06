package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.response.ProgramExerciseResponse;
import com.example.viegymapp.entity.ProgramExercise;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ExerciseMapper.class})
public interface ProgramExerciseMapper {

    @Mapping(target = "exercise", source = "exercise")
    ProgramExerciseResponse toResponse(ProgramExercise entity);
}
