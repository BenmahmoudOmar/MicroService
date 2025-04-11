package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.entities.MessageStatus;
import com.esprit.internify.entities.User;
import com.esprit.internify.repository.ConversationRepository;
import com.esprit.internify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService{
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
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
    public List<Conversation> getUserConversationsSortedByLastMessage(Long userId) {
        // Fetch conversations for the user
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        conversations.sort(Comparator.comparing((Conversation c) -> !c.getUserFavorites().contains(user)).reversed()
                .thenComparing(Conversation::getLastMessageTimestamp).reversed());
        return conversations;
    }

    @Override
    public Conversation toggleFavorite(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Toggle the favorite status for the specific user
        if (conversation.getUserFavorites().contains(user)) {
            // If the user is already a favorite, remove them
            conversation.getUserFavorites().remove(user);
        } else {
            // If the user is not a favorite, add them
            conversation.getUserFavorites().add(user);
        }

        // Save the updated conversation
        Conversation updatedConversation = conversationRepository.save(conversation);

        // Send the updated conversation to the WebSocket topic
        messagingTemplate.convertAndSend("/topic/conversations", updatedConversation);

        return updatedConversation;
    }

    @Override
    public Conversation toggleMute(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User  not found"));

        // Toggle the mute status for the specific user
        if (conversation.getMutedBy().contains(user)) {
            // If the user is already muted, unmute them
            conversation.getMutedBy().remove(user);
        } else {
            // If the user is not muted, mute them
            conversation.getMutedBy().add(user);
        }

        // Save the updated conversation
        Conversation updatedConversation = conversationRepository.save(conversation);

        // Send the updated conversation to the WebSocket topic
        messagingTemplate.convertAndSend("/topic/conversations", updatedConversation);

        return updatedConversation;
    }

    @Override
    public List<Message> searchMessagesInConversation(Long conversationId, String content, String sentDateStr) {
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        if (sentDateStr != null && !sentDateStr.isEmpty()) {
            if (sentDateStr.endsWith("Z")) {
                sentDateStr = sentDateStr.replace("Z", "");
            }
            LocalDateTime parsedDate = LocalDateTime.parse(sentDateStr);
            startOfDay = parsedDate.toLocalDate().atStartOfDay();
            endOfDay = startOfDay.plusDays(1).minusNanos(1); // Covers full day
        }

        return conversationRepository.searchMessagesInConversation(conversationId, content, startOfDay, endOfDay);
    }

}
