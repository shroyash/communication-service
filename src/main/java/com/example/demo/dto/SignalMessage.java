package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {

    public enum Type {
        JOIN,       // user joins room
        OFFER,      // SDP offer
        ANSWER,     // SDP answer
        ICE,        // ICE candidate
        LEAVE,      // user leaves
        BUSY        // other user already in call
    }

    private Type type;
    private Long appointmentId;
    private UUID senderId;
    private UUID targetId;      // who to forward to
    private String sdp;         // for OFFER and ANSWER
    private Object candidate;   // for ICE candidates
}