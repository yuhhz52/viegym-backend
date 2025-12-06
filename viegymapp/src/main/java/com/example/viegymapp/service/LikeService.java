package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.LikeUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final RabbitTemplate rabbitTemplate;

    public void broadcastLikeUpdate(UUID postId, Long likeCount) {
        // Chỉ broadcast likeCount, không broadcast isLikedByCurrentUser
        // Mỗi client tự quản lý trạng thái liked của riêng mình
        LikeUpdateMessage message = LikeUpdateMessage.builder()
                .postId(postId.toString())  // Convert UUID to String for JSON serialization
                .likeCount(likeCount)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.LIKE_QUEUE, message);
    }
}
