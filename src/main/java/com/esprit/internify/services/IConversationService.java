package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;

import java.util.List;
import java.util.Optional;

public interface IConversationService {
    Conversation createConversation(Conversation conversation);
    Optional<Conversation> getConversationById(Long id);
    void deleteConversation(Long id);
    List<Message> getMessagesByConversation(Long conversationId);
    List<Conversation> getUserConversationsSortedByLastMessage(Long userId);
    Conversation toggleFavorite(Long conversationId, Long userId);
}
