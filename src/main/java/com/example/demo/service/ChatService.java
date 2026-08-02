package com.example.demo.service;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.ChatMessageResponseDto;
import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageResponseDto saveAndBroadcast(ChatMessageDto dto, UUID senderId) {
        ChatMessage msg = ChatMessage.builder()
                .appointmentId(dto.getAppointmentId())
                .senderId(senderId)
                .senderName(dto.getSenderName())
                .content(dto.getContent())
                .type(dto.getType())
                .timestamp(Instant.now())
                .build();

        ChatMessage saved = repository.save(msg);

        ChatMessageResponseDto response = ChatMessageResponseDto.builder()
                .id(saved.getId())
                .appointmentId(saved.getAppointmentId())
                .senderId(saved.getSenderId())
                .senderName(saved.getSenderName())
                .content(saved.getContent())
                .type(saved.getType())
                .timestamp(saved.getTimestamp())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/appointment." + saved.getAppointmentId(),
                response
        );

        return response;
    }

    public List<ChatMessage> getMessages(Long appointmentId) {
        return repository.findByAppointmentIdOrderByTimestampAsc(appointmentId);
    }
}