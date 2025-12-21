package com.example.demo.model;

import lombok.Data;
import java.util.Map;

@Data
public class WebRtcMessage {

    public enum WebRtcMessageType {
        JOIN, OFFER, ANSWER, ICE
    }

    private WebRtcMessageType type;   // safer than String
    private Long appointmentId;       // room id
    private String senderId;          // filled server-side
    private String targetId;          // receiver user id
    private Map<String, Object> payload; // SDP or ICE candidate
}
