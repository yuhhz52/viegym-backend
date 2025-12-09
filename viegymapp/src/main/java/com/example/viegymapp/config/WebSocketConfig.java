package com.example.viegymapp.config;

import com.example.viegymapp.security.jwt.JwtUtils;
import com.example.viegymapp.service.Impl.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitHost;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitPort;
    
    @Value("${spring.rabbitmq.stomp.port:61613}")
    private int rabbitStompPort;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitUsername;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String rabbitPassword;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String rabbitVirtualHost;

    @Autowired(required = false)
    private JwtUtils jwtUtils;

    @Autowired(required = false)
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use RabbitMQ STOMP broker for scalable WebSocket across multiple servers
        // This allows WebSocket messages to be shared across multiple application instances
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(rabbitHost)
                .setRelayPort(rabbitStompPort)
                .setClientLogin(rabbitUsername)
                .setClientPasscode(rabbitPassword)
                .setVirtualHost(rabbitVirtualHost)
                .setSystemLogin(rabbitUsername)
                .setSystemPasscode(rabbitPassword)
                .setSystemHeartbeatSendInterval(20000)
                .setSystemHeartbeatReceiveInterval(20000);
        
        config.setApplicationDestinationPrefixes("/app");
        
        logger.info("WebSocket configured with RabbitMQ STOMP broker at {}:{}", rabbitHost, rabbitStompPort);
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
                if (jwtUtils != null && userDetailsService != null) {
                    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                    
                    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                        // Extract token from headers
                        String token = null;
                        
                        // Try to get token from Authorization header
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
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
                        if (token != null && !token.isEmpty() && jwtUtils.validateJwtToken(token)) {
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
                                logger.error("WebSocket authentication failed: {}", e.getMessage());
                                // If authentication fails, the connection will be rejected
                            }
                        } else {
                            logger.warn("WebSocket connection attempt without valid token");
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
