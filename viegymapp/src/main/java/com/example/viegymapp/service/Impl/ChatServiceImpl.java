package com.example.viegymapp.service.Impl;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.request.ChatMessageRequest;
import com.example.viegymapp.dto.response.ChatMessageResponse;
import com.example.viegymapp.entity.ChatMessage;
import com.example.viegymapp.entity.Notification;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.ChatMessageMapper;
import com.example.viegymapp.repository.ChatMessageRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.AsyncNotificationService;
import com.example.viegymapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper messageMapper;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final AsyncNotificationService asyncNotificationService;
    
    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        User sender = getCurrentUser();
        
        User receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        ChatMessage message = messageMapper.toEntity(request);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.now());
        
        message = messageRepository.save(message);
        ChatMessageResponse response = messageMapper.toResponse(message);
        
        // Broadcast directly via WebSocket (simpler and more reliable)
        try {
            messagingTemplate.convertAndSend("/topic/chat/" + response.getReceiverId(), response);
            messagingTemplate.convertAndSend("/topic/chat/" + response.getSenderId(), response);
            log.info("Message broadcasted via WebSocket: {}", response.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast message via WebSocket", e);
        }
        
        // Also publish to RabbitMQ for persistence/async processing
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                RabbitMQConfig.CHAT_ROUTING_KEY,
                response
            );
            log.info("Message published to RabbitMQ: {}", response.getId());
        } catch (Exception e) {
            log.error("Failed to publish message to RabbitMQ", e);
        }
        
        // NOTE: Removed email notification for chat messages
        // Chat messages use real-time WebSocket only, no email needed
        
        return response;
    }
    
    @Override
    public List<ChatMessageResponse> getMyMessages() {
        User currentUser = getCurrentUser();
        List<ChatMessage> messages = messageRepository.findMessagesByUser(currentUser.getId());
        return messages.stream()
            .map(messageMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChatMessageResponse> getConversationWith(UUID userId) {
        User currentUser = getCurrentUser();
        List<ChatMessage> messages = messageRepository.findConversationBetweenUsers(
            currentUser.getId(), userId);
        return messages.stream()
            .map(messageMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public ChatMessageResponse markAsRead(UUID messageId) {
        User currentUser = getCurrentUser();
        
        ChatMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        // Only receiver can mark as read
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        message = messageRepository.save(message);
        return messageMapper.toResponse(message);
    }
    
    @Override
    public List<ChatMessageResponse> getUnreadMessages() {
        User currentUser = getCurrentUser();
        List<ChatMessage> messages = messageRepository.findUnreadMessagesByUser(currentUser.getId());
        return messages.stream()
            .map(messageMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Long getUnreadCount() {
        User currentUser = getCurrentUser();
        return messageRepository.countUnreadMessagesByUser(currentUser.getId());
    }
    
    @Override
    public void deleteConversation(UUID userId) {
        User currentUser = getCurrentUser();
        messageRepository.deleteConversationBetweenUsers(currentUser.getId(), userId);
        log.info("Deleted conversation between user {} and user {}", currentUser.getId(), userId);
    }
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
