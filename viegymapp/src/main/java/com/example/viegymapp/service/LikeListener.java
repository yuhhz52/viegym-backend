package com.example.viegymapp.service;

import com.example.viegymapp.config.RabbitMQConfig;
import com.example.viegymapp.dto.message.LikeUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeListener {

    private final SimpMessagingTemplate messagingTemplate;
    
    // Cache để track recent broadcasts và tránh duplicate
    private final ConcurrentHashMap<String, Long> recentBroadcasts = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_TIME_MS = 100; // 100ms debounce

    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE)
    public void listen(LikeUpdateMessage message) {
        try {
            String cacheKey = message.getPostId() + ":" + message.getLikeCount();
            long now = System.currentTimeMillis();
            
            // Kiểm tra xem message tương tự đã được broadcast gần đây chưa
            Long lastBroadcastTime = recentBroadcasts.get(cacheKey);
            if (lastBroadcastTime != null && (now - lastBroadcastTime) < DEBOUNCE_TIME_MS) {
                return;
            }
            
            // Push chỉ likeCount tới tất cả clients
            String destination = "/topic/likes/" + message.getPostId();
            log.debug("Broadcasting like update to: {} | Count: {}", destination, message.getLikeCount());
            
            messagingTemplate.convertAndSend(destination, message);
            
            // Cache broadcast time
            recentBroadcasts.put(cacheKey, now);
            
            // Cleanup old cache entries (older than 1 second)
            recentBroadcasts.entrySet().removeIf(entry -> 
                (now - entry.getValue()) > TimeUnit.SECONDS.toMillis(1)
            );
            
            log.debug("Broadcast complete for post: {}", message.getPostId());
        } catch (Exception e) {
            log.error("Error broadcasting like update for post: {}", message.getPostId(), e);
        }
    }
}
