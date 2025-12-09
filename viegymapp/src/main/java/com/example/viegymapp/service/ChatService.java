package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.ChatMessageRequest;
import com.example.viegymapp.dto.response.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    
    ChatMessageResponse sendMessage(ChatMessageRequest request);
    List<ChatMessageResponse> getMyMessages();
    List<ChatMessageResponse> getConversationWith(UUID userId);
    ChatMessageResponse markAsRead(UUID messageId);
    List<ChatMessageResponse> getUnreadMessages();
    Long getUnreadCount();
    void deleteConversation(UUID userId);
}
