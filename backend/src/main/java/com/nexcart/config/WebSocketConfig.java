package com.nexcart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.nexcart.security.StompAuthorizationInterceptor;
import com.nexcart.security.StompJwtAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtAuthInterceptor stompJwtAuthInterceptor;
    private final StompAuthorizationInterceptor stompAuthorizationInterceptor;

    public WebSocketConfig(
            StompJwtAuthInterceptor stompJwtAuthInterceptor,
            StompAuthorizationInterceptor stompAuthorizationInterceptor) {
        this.stompJwtAuthInterceptor = stompJwtAuthInterceptor;
        this.stompAuthorizationInterceptor = stompAuthorizationInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://127.0.0.1:5500");
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {
        registration.interceptors(
                stompJwtAuthInterceptor,
                stompAuthorizationInterceptor
        );
    }
}