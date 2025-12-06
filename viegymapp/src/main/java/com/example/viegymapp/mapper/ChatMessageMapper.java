package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.ChatMessageRequest;
import com.example.viegymapp.dto.response.ChatMessageResponse;
import com.example.viegymapp.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "type", constant = "TEXT")
    ChatMessage toEntity(ChatMessageRequest request);
    
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "receiver.fullName", target = "receiverName")
    ChatMessageResponse toResponse(ChatMessage entity);
}
