package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get all messages for a particular appointment, sorted by timestamp
    List<ChatMessage> findByAppointmentIdOrderByTimestampAsc(Long appointmentId);

    // Optional: get all messages between sender and receiver for an appointment
    List<ChatMessage> findByAppointmentIdAndSenderIdAndReceiverIdOrderByTimestampAsc(
            Long appointmentId, UUID senderId, UUID receiverId
    );
}
