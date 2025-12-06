package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.CommentBroadcastMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void listen(CommentBroadcastMessage message) {
        // Push comment tới client đang subscribe theo postId
        messagingTemplate.convertAndSend("/topic/comments/" + message.getPostId(), message.getComment());
    }
}
