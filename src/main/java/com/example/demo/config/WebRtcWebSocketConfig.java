package com.example.demo.config;

import com.example.demo.service.WebRtcHandler;
import com.example.demo.websocket.CustomHandshakeHandler;
import com.example.demo.websocket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebRtcWebSocketConfig implements WebSocketConfigurer {

    private final SimpMessagingTemplate messagingTemplate;

    @Bean
    public WebSocketHandler webRtcHandler() {
        return new WebRtcHandler(messagingTemplate);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(webRtcHandler(), "/webrtc")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor())
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS()
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);
    }
}
