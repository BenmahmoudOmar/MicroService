package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IConversationService {
    Conversation createConversation(Conversation conversation);
    Optional<Conversation> getConversationById(Long id);
    void deleteConversation(Long id);
    List<Message> getMessagesByConversation(Long conversationId);
    List<Conversation> getUserConversationsSortedByLastMessage(Long userId);
    Conversation toggleFavorite(Long conversationId, Long userId);
    Conversation toggleMute(Long conversationId, Long userId);
    List<Message> searchMessagesInConversation(Long conversationId, String content, String sentDateStr);
    List<Conversation> getAllConversations();
    Map<String, Object> getConversationStats(Long conversationId);
}
