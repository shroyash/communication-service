package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.MessageType;
import com.example.demo.model.WebRtcMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class WebRtcHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> rooms = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebRtcMessage msg = objectMapper.readValue(message.getPayload(), WebRtcMessage.class);

        // Use senderId from frontend or fallback
        String userId = msg.getSenderId();
        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }
        msg.setSenderId(userId);
        userSessions.put(userId, session);

        log.info("Signaling received: type={} appointment={} from={} target={}",
                msg.getType(), msg.getAppointmentId(), msg.getSenderId(), msg.getTargetId());

        switch (msg.getType()) {
            case JOIN -> handleJoin(msg);
            case OFFER, ANSWER, ICE -> forwardToTarget(msg);
            default -> log.warn("Unknown signaling type: {}", msg.getType());
        }
    }

    private void handleJoin(WebRtcMessage msg) {
        rooms.putIfAbsent(msg.getAppointmentId(), ConcurrentHashMap.newKeySet());
        Set<String> users = rooms.get(msg.getAppointmentId());

        if (users.add(msg.getSenderId())) {
            ChatMessage joinMsg = ChatMessage.builder()
                    .appointmentId(msg.getAppointmentId())
                    .senderId("SYSTEM")
                    .content(msg.getSenderId() + " joined the appointment")
                    .type(MessageType.NOTIFICATION)
                    .timestamp(Instant.now())
                    .build();
            messagingTemplate.convertAndSend("/topic/appointment." + msg.getAppointmentId(), joinMsg);
        }
    }

    private void forwardToTarget(WebRtcMessage msg) throws Exception {
        String target = msg.getTargetId();
        if (target == null) return;

        WebSocketSession targetSession = userSessions.get(target);
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String disconnectedUser = null;
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                disconnectedUser = entry.getKey();
                userSessions.remove(entry.getKey());
                break;
            }
        }

        if (disconnectedUser != null) {
            String finalDisconnectedUser = disconnectedUser;
            rooms.forEach((appointmentId, users) -> {
                if (users.remove(finalDisconnectedUser)) {
                    ChatMessage leaveMsg = ChatMessage.builder()
                            .appointmentId(appointmentId)
                            .senderId("SYSTEM")
                            .content(finalDisconnectedUser + " left the appointment")
                            .type(MessageType.NOTIFICATION)
                            .timestamp(Instant.now())
                            .build();
                    messagingTemplate.convertAndSend("/topic/appointment." + appointmentId, leaveMsg);
                }
            });
        }
    }
}
