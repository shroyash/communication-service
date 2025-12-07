package com.example.demo.service;


import com.example.demo.model.WebRtcMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebRtcHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Map userId -> WebSocketSession
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // Map appointmentId -> Set<userId>
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebRTC connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebRtcMessage msg = objectMapper.readValue(message.getPayload(), WebRtcMessage.class);

        // Get user from Principal set by handshake handler
        String userId = Optional.ofNullable(session.getPrincipal())
                .map(java.security.Principal::getName)
                .orElse(null);

        if (userId == null) {
            log.warn("Unauthenticated session tried to send signaling");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not authenticated"));
            return;
        }

        // server sets senderId reliably (do not trust client)
        msg.setSenderId(userId);
        userSessions.put(userId, session);

        log.info("Signaling received: type={} appointment={} from={} target={}",
                msg.getType(), msg.getAppointmentId(), msg.getSenderId(), msg.getTargetId());

        switch (msg.getType()) {
            case "join" -> handleJoin(msg);
            case "offer", "answer", "ice" -> forwardToTarget(msg);
            default -> log.warn("Unknown signaling type: {}", msg.getType());
        }
    }

    private void handleJoin(WebRtcMessage msg) {
        rooms.putIfAbsent(msg.getAppointmentId(), ConcurrentHashMap.newKeySet());
        rooms.get(msg.getAppointmentId()).add(msg.getSenderId());
        log.info("User {} joined room {}", msg.getSenderId(), msg.getAppointmentId());
    }

    private void forwardToTarget(WebRtcMessage msg) {
        String target = msg.getTargetId();
        if (target == null) {
            log.warn("No target specified for message: {}", msg);
            return;
        }
        WebSocketSession targetSession = userSessions.get(target);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
                log.info("Forwarded {} from {} to {}", msg.getType(), msg.getSenderId(), target);
            } catch (Exception e) {
                log.error("Failed to forward to target {}", target, e);
            }
        } else {
            log.warn("Target {} not connected", target);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // remove session from userSessions and rooms
        userSessions.values().removeIf(s -> s.getId().equals(session.getId()));
        rooms.values().forEach(set -> set.removeIf(uid -> userSessions.get(uid) == null));
        log.info("WebRTC session closed: {} status={}", session.getId(), status);
    }
}
