package com.example.demo.config;

import com.example.demo.service.WebRtcHandler;
import com.example.demo.websocket.CustomHandshakeHandler;
import com.example.demo.websocket.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebRtcWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebRtcHandler(), "/webrtc")
                .setAllowedOrigins("*")
                .addInterceptors(new JwtHandshakeInterceptor())
                .setHandshakeHandler(new CustomHandshakeHandler());
    }
}
