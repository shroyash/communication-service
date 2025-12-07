package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebRtcSignalController {

    @MessageMapping("/signal/{userId}")
    @SendToUser("/queue/signal")
    public Map<String, Object> signaling(
            @DestinationVariable String userId,
            Map<String, Object> signalData
    ) {
        return signalData; // forward WebRTC offer/answer/candidate
    }
}
