package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.ChatMessageRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.ChatMessageResponse;
import com.example.viegymapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        return ApiResponse.<ChatMessageResponse>builder()
            .result(chatService.sendMessage(request))
            .build();
    }
    
    @GetMapping("/messages/my")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<List<ChatMessageResponse>> getMyMessages() {
        return ApiResponse.<List<ChatMessageResponse>>builder()
            .result(chatService.getMyMessages())
            .build();
    }
    
    @GetMapping("/messages/conversation/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<List<ChatMessageResponse>> getConversationWith(@PathVariable UUID userId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
            .result(chatService.getConversationWith(userId))
            .build();
    }
    
    @PutMapping("/messages/{messageId}/read")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<ChatMessageResponse> markAsRead(@PathVariable UUID messageId) {
        return ApiResponse.<ChatMessageResponse>builder()
            .result(chatService.markAsRead(messageId))
            .build();
    }
    
    @GetMapping("/messages/unread")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<List<ChatMessageResponse>> getUnreadMessages() {
        return ApiResponse.<List<ChatMessageResponse>>builder()
            .result(chatService.getUnreadMessages())
            .build();
    }
    
    @GetMapping("/messages/unread/count")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.<Long>builder()
            .result(chatService.getUnreadCount())
            .build();
    }
    
    @DeleteMapping("/messages/conversation/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'COACH')")
    public ApiResponse<Void> deleteConversation(@PathVariable UUID userId) {
        chatService.deleteConversation(userId);
        return ApiResponse.<Void>builder().build();
    }
    
    // WebSocket endpoints
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessageResponse send(@Payload ChatMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        return chatService.sendMessage(request);
    }
}
