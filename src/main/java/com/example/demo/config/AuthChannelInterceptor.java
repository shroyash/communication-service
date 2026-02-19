package com.example.demo.config;

import com.example.demo.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Read JWT from the cookie
            String token = null;
            String cookieHeader = accessor.getFirstNativeHeader("cookie");
            if (cookieHeader != null) {
                for (String cookie : cookieHeader.split(";")) {
                    String[] parts = cookie.trim().split("=");
                    if (parts.length == 2 && parts[0].equals("jwt")) {
                        token = parts[1];
                        break;
                    }
                }
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            accessor.getSessionAttributes().put("userId",
                    jwtUtil.extractUserId(token));
        }

        return message;
    }
}
