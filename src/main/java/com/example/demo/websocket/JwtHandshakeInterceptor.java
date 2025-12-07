package com.example.demo.websocket;

import com.example.demo.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest sreq) {
            HttpServletRequest servlet = sreq.getServletRequest();
            String token = null;
            String authHeader = servlet.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                Cookie cookie = WebUtils.getCookie(servlet, "access_token");
                if (cookie != null) token = cookie.getValue();
            }
            if (token == null || token.isBlank()) return false;
            if (!jwtUtil.validateToken(token)) return false;
            String userId = jwtUtil.extractUserId(token);
            if (userId == null) return false;
            attributes.put("user", userId);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) { }
}
