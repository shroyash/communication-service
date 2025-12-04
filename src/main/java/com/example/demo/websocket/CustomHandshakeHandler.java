package com.example.demo.websocket;
import com.example.demo.websocket.StompPrincipal;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object user = attributes.get("user");
        if (user instanceof String) return new StompPrincipal((String) user);
        return super.determineUser(request, wsHandler, attributes);
    }
}