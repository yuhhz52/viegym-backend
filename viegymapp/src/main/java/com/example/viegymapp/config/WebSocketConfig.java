package com.example.viegymapp.config;

import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.service.Impl.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    // Note: These RabbitMQ configs are not used - we use enableSimpleBroker (in-memory)
    // Keeping them for future reference if we need to switch to RabbitMQ STOMP relay
    
    @Autowired(required = false)
    private JwtUtils jwtUtils;

    @Autowired(required = false)
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker("/topic", "/queue");
        
        config.setApplicationDestinationPrefixes("/app");
        
        logger.info("WebSocket configured with simple in-memory message broker");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    
                    // Handle CONNECT command - authentication
                    if (StompCommand.CONNECT.equals(command)) {
                        if (jwtUtils == null || userDetailsService == null) {
                            logger.debug("WebSocket CONNECT: JWT utils or user service not available, allowing connection");
                            return message;
                        }
                        
                        // Extract token from headers
                        String token = null;
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        
                        // Try to get token from Authorization header
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }
                        
                        // If not found, try to get from cookie header (for web clients)
                        if (token == null || token.isEmpty()) {
                            String cookieHeader = accessor.getFirstNativeHeader("Cookie");
                            if (cookieHeader != null) {
                                // Parse cookie header to find JWT cookie
                                String[] cookies = cookieHeader.split(";");
                                for (String cookie : cookies) {
                                    cookie = cookie.trim();
                                    if (cookie.startsWith(jwtUtils.getJwtCookieName() + "=")) {
                                        token = cookie.substring((jwtUtils.getJwtCookieName() + "=").length());
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Validate token and set authentication
                        if (token != null && !token.isEmpty()) {
                            if (jwtUtils.validateJwtToken(token)) {
                                try {
                                    String username = jwtUtils.getUserNameFromJwtToken(token);
                                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                    
                                    UsernamePasswordAuthenticationToken authentication = 
                                        new UsernamePasswordAuthenticationToken(
                                            userDetails, 
                                            null, 
                                            userDetails.getAuthorities()
                                        );
                                    
                                    accessor.setUser(authentication);
                                    logger.debug("WebSocket authentication successful for user: {}", username);
                                } catch (Exception e) {
                                    logger.warn("WebSocket authentication failed for token: {} - Error: {}", 
                                            token.substring(0, Math.min(20, token.length())) + "...", e.getMessage());
                                    // Connection will continue but without authentication
                                }
                            } else {
                                // Token exists but is invalid (expired, malformed, etc.)
                                logger.debug("WebSocket connection with invalid token (expired or malformed)");
                                // Allow connection but it will be unauthenticated
                            }
                        } else {
                            // No token provided - this is normal for:
                            // - Health checks
                            // - Retry attempts
                            // - Clients not yet authenticated
                            logger.debug("WebSocket connection attempt without token (anonymous connection)");
                            // Allow connection but it will be unauthenticated
                        }
                    }
                    
                    // Handle SUBSCRIBE command - fix destination prefix and check authentication
                    if (StompCommand.SUBSCRIBE.equals(command)) {
                        String destination = accessor.getDestination();
                        
                        // Check if user is authenticated for protected destinations
                        if (accessor.getUser() == null) {
                            // User is not authenticated - only allow public topics if any
                            // For now, we'll allow subscription but the actual message sending will be protected
                            logger.debug("WebSocket SUBSCRIBE to {} without authentication", destination);
                        }
                        
                        if (destination != null && !destination.startsWith("/topic/") && !destination.startsWith("/queue/") && !destination.startsWith("/user/")) {
                            // Fix destinations that are missing /topic prefix
                            // Common patterns: /chat/, /notifications/, /likes/, etc.
                            if (destination.startsWith("/chat/") || 
                                destination.startsWith("/notifications/") || 
                                destination.startsWith("/likes/") ||
                                destination.startsWith("/comments/")) {
                                String correctedDestination = "/topic" + destination;
                                accessor.setDestination(correctedDestination);
                                logger.debug("Fixed subscription destination: {} -> {}", destination, correctedDestination);
                            } else {
                                // Log unexpected destination format for debugging
                                logger.debug("Subscription to unexpected destination format: {}", destination);
                            }
                        }
                    }
                }
                
                return message;
            }
        });
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Ensure proper JSON serialization for WebSocket messages
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        messageConverters.add(converter);
        return false; // false = add default converters after custom ones
    }

}
