package com.example.demo.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Extracts the JWT cookie during the HTTP WebSocket upgrade handshake
 * and stores it in the WebSocket session attributes.
 *
 * This is necessary because HttpOnly cookies are sent with the HTTP upgrade
 * request but are NOT accessible inside the STOMP CONNECT frame.
 * AuthChannelInterceptor then reads it from session attributes.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // Extract cookies from the HTTP upgrade request headers
        String cookieHeader = request.getHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].trim().equals("jwt")) {
                    // Store in session attributes — accessible in AuthChannelInterceptor
                    attributes.put("jwt", parts[1].trim());
                    break;
                }
            }
        }

        return true; // Always allow the handshake; auth is enforced in AuthChannelInterceptor
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // Nothing to do after handshake
    }
}