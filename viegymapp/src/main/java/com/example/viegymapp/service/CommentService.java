package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.CommentBroadcastMessage;
import com.example.viegymapp.dto.response.PostCommentResponse;
import com.example.viegymapp.entity.PostComment;
import com.example.viegymapp.mapper.CommunityMapper;
import com.example.viegymapp.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final RabbitTemplate rabbitTemplate;
    private final CommentRepository commentRepository;
    private final CommunityMapper communityMapper;

    public PostCommentResponse saveAndPublish(PostComment comment) {
        // 1. Lưu comment vào DB
        PostComment saved = commentRepository.saveAndFlush(comment);
        PostCommentResponse payload = communityMapper.toResponse(saved);

        // 2. Gửi message tới RabbitMQ
        CommentBroadcastMessage message = CommentBroadcastMessage.builder()
                .postId(saved.getPost().getId())
                .comment(payload)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.COMMENT_QUEUE, message);
        return payload;
    }
}
