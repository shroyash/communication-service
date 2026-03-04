package com.example.demo.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketAuthHelper {

    public UUID extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        // userId already set in session by AuthChannelInterceptor at CONNECT time
        // no JWT parsing needed here — already validated once
        String userId = (String) headerAccessor
                .getSessionAttributes()
                .get("userId");

        if (userId == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        return UUID.fromString(userId);
    }
}