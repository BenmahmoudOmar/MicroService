package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.entities.MessageStatus;
import com.esprit.internify.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Map<String, Object> message = new HashMap<>();
        message.put("action", "deleted");
        message.put("id", id);
        messagingTemplate.convertAndSend("/topic/conversations", message);
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
    @Transactional // Ensure this method runs in a transaction
    public List<Conversation> getUserConversationsSortedByLastMessage(Long userId) {
        // Fetch conversations for the user
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimestamp(userId);

        // Update unreadMessagesCount for each conversation
        for (Conversation conversation : conversations) {
            // Count SENT messages
            int sentMessagesCount = (int) conversation.getMessages().stream()
                    .filter(message -> message.getStatus() == MessageStatus.SENT && message.getReceiver().getId() == userId) // Assuming MessageStatus is an enum
                    .count();

            // Update unreadMessagesCount
            conversation.setUnreadMessagesCount(sentMessagesCount);

            // Save the updated conversation
            conversationRepository.save(conversation); // Save the updated conversation
        }
        return conversations;
    }
}
