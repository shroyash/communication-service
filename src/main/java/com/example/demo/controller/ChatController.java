package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) throw new IllegalArgumentException("User not authenticated");

        message.setSenderId(UUID.fromString(userId));
        message.setTimestamp(Instant.now());

        ChatMessage saved = repository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/appointment." + saved.getAppointmentId(),
                saved
        );
    }

    // KEY FIX: path must match what the gateway routes as /api/communication/**
    // Frontend calls: /api/communication/appointments/{id}/messages
    @GetMapping("/api/communication/appointments/{id}/messages")
    public List<ChatMessage> getMessages(@PathVariable Long id) {
        return repository.findByAppointmentIdOrderByTimestampAsc(id);
    }
}