package com.example.demo.controller;

import com.example.demo.dto.SignalMessage;
import com.example.demo.helper.WebSocketAuthHelper;
import com.example.demo.service.WebRtcRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebRtcSignalingController {

    private final WebRtcRoomService webRtcRoomService;
    private final WebSocketAuthHelper webSocketAuthHelper;

    @MessageMapping("/webrtc.signal")
    public void handleSignal(
            SignalMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        UUID senderId = webSocketAuthHelper.extractUserId(headerAccessor);
        message.setSenderId(senderId); // always set from session, never trust client

        log.info("Signal received: type={} from={} appointment={}",
                message.getType(), senderId, message.getAppointmentId());

        switch (message.getType()) {
            case JOIN   -> webRtcRoomService.handleJoin(message);
            case OFFER  -> webRtcRoomService.handleOffer(message);
            case ANSWER -> webRtcRoomService.handleAnswer(message);
            case ICE    -> webRtcRoomService.handleIceCandidate(message);
            case LEAVE  -> webRtcRoomService.handleLeave(message);
            default     -> log.warn("Unknown signal type: {}", message.getType());
        }
    }

    // REST endpoint — give frontend the STUN/TURN config
    @GetMapping("/api/communication/webrtc/ice-servers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getIceServers() {
        List<Map<String, Object>> iceServers = List.of(
                Map.of("urls", "stun:stun.l.google.com:19302"),       // free Google STUN
                Map.of("urls", "stun:stun1.l.google.com:19302")       // backup STUN
                // Add TURN server here when you have one for production
        );
        return ResponseEntity.ok(iceServers);
    }
}