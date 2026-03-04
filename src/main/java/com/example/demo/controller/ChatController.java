package com.example.demo.controller;

import com.example.demo.helper.WebSocketAuthHelper;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final WebSocketAuthHelper webSocketAuthHelper;

    @MessageMapping("/chat.send")
    public void sendMessage(
            ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        UUID senderId = webSocketAuthHelper.extractUserId(headerAccessor);
        chatService.saveAndBroadcast(message, senderId);
    }

    @GetMapping("/api/communication/appointments/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }
}