    package com.example.demo.service;

    import com.example.demo.model.ChatMessage;
    import com.example.demo.repository.ChatMessageRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.stereotype.Service;

    import java.time.Instant;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class ChatService {

        private final ChatMessageRepository repository;
        private final SimpMessagingTemplate messagingTemplate;

        public ChatMessage saveAndBroadcast(ChatMessage msg) {
            msg.setTimestamp(Instant.now());
            ChatMessage saved = repository.save(msg);

            // Broadcast to /topic/appointment.{id}
            messagingTemplate.convertAndSend("/topic/appointment." + saved.getAppointmentId(), saved);
            return saved;
        }

        public List<ChatMessage> getMessages(Long appointmentId) {
            return repository.findByAppointmentIdOrderByTimestampAsc(appointmentId);
        }
    }
