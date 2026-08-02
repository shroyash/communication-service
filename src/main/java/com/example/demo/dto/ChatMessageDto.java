package com.example.demo.dto;

import com.example.demo.model.MessageType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private Long appointmentId;
    private String content;
    private MessageType type;
    private String senderName;
}