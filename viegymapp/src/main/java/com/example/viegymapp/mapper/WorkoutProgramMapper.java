package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.response.WorkoutProgramResponse;
import com.example.viegymapp.entity.WorkoutProgram;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ProgramExerciseMapper.class})
public interface WorkoutProgramMapper {

    @Mapping(target = "exercises", source = "programExercises")
    @Mapping(target = "createdBy", source = "creator.id")
    @Mapping(target = "createdByName", source = "creator.fullName")
    WorkoutProgramResponse toResponse(WorkoutProgram entity);
}
