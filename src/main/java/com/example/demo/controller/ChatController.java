package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(ChatMessage incoming, Principal principal) {
        if (principal == null) return;
        // Server trusts the Principal, not client-provided senderId
        incoming.setSenderId(principal.getName());
        chatService.saveAndBroadcast(incoming);
    }
    @GetMapping("/{appointmentId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable Long appointmentId,
            @RequestHeader(value = "userId", required = false) String userId
    ) {
        log.info("📜 GET message history request: appointmentId={}, userId={}",
                appointmentId, userId);

        // TODO: Add authorization check
        // Verify that userId is either the doctor or patient in this appointment
        List<ChatMessage> messages = chatService.getMessageHistory(appointmentId);

        log.info("✅ Returning {} messages for appointment {}",
                messages.size(), appointmentId);

        return ResponseEntity.ok(messages);
    }
}