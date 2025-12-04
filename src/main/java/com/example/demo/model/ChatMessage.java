package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.awt.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {@Index(name="idx_appointment_ts", columnList = "appointmentId, timestamp")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String appointmentId;

    @Column(nullable = false)
    private String senderId;

    @Column
    private String receiverId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();
}