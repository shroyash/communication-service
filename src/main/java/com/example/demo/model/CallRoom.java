package com.example.demo.model;

import lombok.Data;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class CallRoom {
    private Long appointmentId;
    private UUID doctorId;
    private UUID patientId;
    private Set<UUID> connectedUsers = ConcurrentHashMap.newKeySet();
    private Instant startedAt;

    public boolean isFull() {
        return connectedUsers.size() >= 2;
    }

    public Optional<UUID> getOtherUser(UUID userId) {
        return connectedUsers.stream()
                .filter(id -> !id.equals(userId))
                .findFirst();
    }
}