package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage saveAndBroadcast(ChatMessage incoming) {
        incoming.setTimestamp(Instant.now());
        ChatMessage saved = repo.save(incoming);
        String destination = "/topic/appointment." + saved.getAppointmentId();
        messagingTemplate.convertAndSend(destination, saved);
        return saved;
    }
}
