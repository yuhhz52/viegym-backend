package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.ExerciseRequest;
import com.example.viegymapp.dto.response.ExerciseResponse;
import com.example.viegymapp.dto.response.ExerciseShortResponse;
import com.example.viegymapp.entity.Exercise;
import com.example.viegymapp.entity.Tag;
import org.mapstruct.*;


import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExerciseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "tags", ignore = true) // giữ mapTagNames nếu muốn chuyển Set<Tag> -> Set<String>
    @Mapping(target = "programExercises", ignore = true)
    @Mapping(target = "sessionLogs", ignore = true)
    Exercise toEntity(ExerciseRequest dto);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagNames")
    @Mapping(target = "exerciseType", expression = "java(entity.getExerciseType() != null ? entity.getExerciseType().name() : null)")
        // mediaList tự map nếu field cùng tên và kiểu tương thích
    ExerciseResponse toResponseDTO(Exercise entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "mediaList", ignore = true)
    @Mapping(target = "programExercises", ignore = true)
    @Mapping(target = "sessionLogs", ignore = true)
    void updateExercise(@MappingTarget Exercise exercise, ExerciseRequest dto);

    ExerciseShortResponse toShortResponse(Exercise entity);

    @Named("mapTagNames")
    default Set<String> mapTagNames(Set<Tag> tags) {
        return tags != null ? tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()) : null;
    }
}
