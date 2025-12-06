package com.example.viegymapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitHost;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitPort;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitUsername;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String rabbitPassword;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use simple in-memory broker (simpler, no RabbitMQ dependency for WebSocket)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Ensure proper JSON serialization for WebSocket messages
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        messageConverters.add(converter);
        return false; // false = add default converters after custom ones
    }

}
