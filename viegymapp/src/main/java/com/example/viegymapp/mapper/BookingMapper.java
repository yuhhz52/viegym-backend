package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.BookingRequest;
import com.example.viegymapp.dto.response.BookingResponse;
import com.example.viegymapp.entity.BookingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TimeSlotMapper.class})
public interface BookingMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "timeSlot", ignore = true)
    @Mapping(target = "bookingTime", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "coachNotes", ignore = true)
    BookingSession toEntity(BookingRequest request);
    
    @Mapping(source = "coach.id", target = "coachId")
    @Mapping(source = "coach.fullName", target = "coachName")
    @Mapping(source = "coach.avatarUrl", target = "coachAvatarUrl")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.fullName", target = "clientName")
    BookingResponse toResponse(BookingSession entity);
}
