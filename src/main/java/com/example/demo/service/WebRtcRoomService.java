package com.example.demo.service;
import com.example.demo.dto.SignalMessage;
import com.example.demo.model.CallRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebRtcRoomService {
    private final Map<Long, CallRoom> activeRooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public void handleJoin(SignalMessage message) {
        Long appointmentId = message.getAppointmentId();
        UUID userId = message.getSenderId();
        CallRoom room = activeRooms.computeIfAbsent(appointmentId, id -> {
            CallRoom newRoom = new CallRoom();
            newRoom.setAppointmentId(id);
            newRoom.setStartedAt(Instant.now());
            return newRoom;
        });
        if (room.isFull()) {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/webrtc",
                    new SignalMessage(SignalMessage.Type.BUSY, appointmentId, null, userId, null, null)
            );
            return;
        }
        room.getConnectedUsers().add(userId);
        log.info("User {} joined room {}", userId, appointmentId);

        room.getOtherUser(userId).ifPresent(otherUserId -> {
            SignalMessage joinNotification = new SignalMessage();
            joinNotification.setType(SignalMessage.Type.JOIN);
            joinNotification.setAppointmentId(appointmentId);
            joinNotification.setSenderId(userId);
            joinNotification.setTargetId(otherUserId);
            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/webrtc",
                    joinNotification
            );
        });
    }

    public void handleOffer(SignalMessage message) {
        forwardToTarget(message);
    }

    public void handleAnswer(SignalMessage message) {
        forwardToTarget(message);
    }

    public void handleIceCandidate(SignalMessage message) {
        forwardToTarget(message);
    }

    public void handleLeave(SignalMessage message) {
        Long appointmentId = message.getAppointmentId();
        UUID userId = message.getSenderId();
        CallRoom room = activeRooms.get(appointmentId);
        if (room == null) return;
        room.getConnectedUsers().remove(userId);
        log.info("User {} left room {}", userId, appointmentId);
        if (room.getConnectedUsers().isEmpty()) {
            activeRooms.remove(appointmentId);
            log.info("Room {} closed", appointmentId);
        }
        room.getOtherUser(userId).ifPresent(otherUserId ->
                messagingTemplate.convertAndSendToUser(
                        otherUserId.toString(),
                        "/queue/webrtc",
                        new SignalMessage(SignalMessage.Type.LEAVE, appointmentId, userId, otherUserId, null, null)
                )
        );
    }

    // ✅ FIX — guard against null targetId instead of letting it throw silently
    private void forwardToTarget(SignalMessage message) {
        if (message.getTargetId() == null) {
            log.warn("Dropping signal {} from {} — no targetId set", message.getType(), message.getSenderId());
            return;
        }
        messagingTemplate.convertAndSendToUser(
                message.getTargetId().toString(),
                "/queue/webrtc",
                message
        );
    }

    public Map<Long, CallRoom> getActiveRooms() {
        return Collections.unmodifiableMap(activeRooms);
    }
}
