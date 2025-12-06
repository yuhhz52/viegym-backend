package com.example.viegymapp.listener;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void handleChatMessage(ChatMessageResponse message) {
        log.info("Received chat message from RabbitMQ: {}", message);
        
        // Send to specific user's queue
        messagingTemplate.convertAndSend(
            "/topic/chat/" + message.getReceiverId(), 
            message
        );
        
        // Also send to sender for confirmation
        messagingTemplate.convertAndSend(
            "/topic/chat/" + message.getSenderId(), 
            message
        );
    }
}
