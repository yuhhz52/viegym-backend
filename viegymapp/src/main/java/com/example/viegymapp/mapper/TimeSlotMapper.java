package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.TimeSlotRequest;
import com.example.viegymapp.dto.response.TimeSlotResponse;
import com.example.viegymapp.entity.CoachTimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper for converting between TimeSlotRequest, CoachTimeSlot, and TimeSlotResponse.
 */
@Mapper(componentModel = "spring")
public interface TimeSlotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "isAvailable", constant = "true")
    @Mapping(target = "status", constant = "AVAILABLE")
    CoachTimeSlot toEntity(TimeSlotRequest request);

    @Mapping(source = "coach.id", target = "coachId")
    @Mapping(source = "coach.fullName", target = "coachName")
    @Mapping(source = "coach.avatarUrl", target = "coachAvatarUrl")
    TimeSlotResponse toResponse(CoachTimeSlot entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coach", ignore = true)
    void updateEntity(TimeSlotRequest request, @MappingTarget CoachTimeSlot entity);
}
