package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public List<ChatMessage> getMessageHistory(Long appointmentId) {
        log.info("📚 Fetching message history for appointment: {}", appointmentId);

        List<ChatMessage> messages = repo.findByAppointmentIdOrderByTimestampAsc(appointmentId);

        log.info("📚 Found {} messages", messages.size());

        return messages;
    }
}