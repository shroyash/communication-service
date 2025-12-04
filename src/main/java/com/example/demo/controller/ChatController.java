package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(ChatMessage incoming, Principal principal) {
        if (principal == null) return;
        // Server trusts the Principal, not client-provided senderId
        incoming.setSenderId(principal.getName());
        chatService.saveAndBroadcast(incoming);
    }
}