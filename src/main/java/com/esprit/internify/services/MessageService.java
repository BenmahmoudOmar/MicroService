package com.esprit.internify.services;

import com.esprit.internify.entities.Message;
import com.esprit.internify.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Message sendMessage(Message message) {
        message.setId(null);
        Message savedMessage = messageRepository.save(message);
        // Émettre le message créé
        messagingTemplate.convertAndSend("/topic/messages",savedMessage);
        return savedMessage;
    }

    @Override
    public Optional<Message> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public Message updateMessage(Long id, String newContent) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setContent(newContent);
        Message updatedMessage = messageRepository.save(message);

        // Émettre un message JSON avec l'ID du message mis à jour
        Map<String, Object> updateMessage = new HashMap<>();
        updateMessage.put("action", "updated");
        updateMessage.put("message", updatedMessage);
        messagingTemplate.convertAndSend("/topic/messages", updateMessage);

        return updatedMessage;
    }

    @Override
    public void deleteMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setContent("Deleted message"); // Marquer comme supprimé
        Message deletedMessage = messageRepository.save(message);

        // Émettre un message JSON avec l'ID du message supprimé
        Map<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("action", "deleted");
        deleteMessage.put("message", deletedMessage);
        messagingTemplate.convertAndSend("/topic/messages", deleteMessage);
    }
}