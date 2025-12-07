package com.example.demo.model;

import lombok.Data;

@Data
public class WebRtcMessage {
    private String type;          // "join" | "offer" | "answer" | "ice"
    private String appointmentId; // room id
    private String senderId;      // filled server-side
    private String targetId;      // receiver user id
    private Object payload;       // sdp or ice candidate
}
