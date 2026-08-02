package com.example.demo.dto;

import com.example.demo.model.MessageType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {

    private Long id;
    private Long appointmentId;
    private UUID senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private Instant timestamp;
}