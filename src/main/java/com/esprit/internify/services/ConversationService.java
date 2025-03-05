package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService{
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Conversation createConversation(Conversation conversation) {
        conversation.setId(null);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageTimestamp(LocalDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);
        messagingTemplate.convertAndSend("/topic/conversations", savedConversation);
        return savedConversation;
    }

    @Override
    public Optional<Conversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    @Override
    public void deleteConversation(Long id) {
        conversationRepository.deleteById(id);
        // Émettre un message JSON avec l'ID de la conversation supprimée
        Map<String, Object> message = new HashMap<>();
        message.put("action", "deleted");
        message.put("id", id);
        messagingTemplate.convertAndSend("/topic/conversations", message); // Émettre l'événement de suppression
    }

    @Override
    public List<Message> getMessagesByConversation(Long conversationId) {
        List<Message> messages = conversationRepository.findById(conversationId)
                .map(Conversation::getMessages)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/messages", messages);
        return messages;
    }

    @Override
    public List<Conversation> getUserConversationsSortedByLastMessage(Long userId) {
        return conversationRepository.findByUserIdOrderByLastMessageTimestamp(userId);
    }
}
