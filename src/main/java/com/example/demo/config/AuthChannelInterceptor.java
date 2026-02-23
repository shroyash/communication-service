package com.example.demo.config;

import com.example.demo.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = null;

        // Strategy 1: Read from STOMP native header "cookie" (sent by some clients)
        String cookieHeader = accessor.getFirstNativeHeader("cookie");
        if (cookieHeader != null) {
            token = extractJwtFromCookieHeader(cookieHeader);
        }

        // Strategy 2: Read from HTTP handshake attributes (set during WebSocket upgrade)
        // This is where the browser's HttpOnly cookie ends up with native WebSocket
        if (token == null) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Object cookieObj = sessionAttributes.get("jwt");
                if (cookieObj instanceof String) {
                    token = (String) cookieObj;
                }
            }
        }

        // Strategy 3: Read from Authorization header (fallback)
        if (token == null) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or missing JWT token");
        }

        accessor.getSessionAttributes().put("userId", jwtUtil.extractUserId(token));
        return message;
    }

    private String extractJwtFromCookieHeader(String cookieHeader) {
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals("jwt")) {
                return parts[1].trim();
            }
        }
        return null;
    }
}