package com.esprit.internify.services;

import com.esprit.internify.entities.Message;
import com.esprit.internify.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Message sendMessage(Message message) {
        Message savedMessage = messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
        return savedMessage;
    }

    @Override
    public Optional<Message> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public Message updateMessage(Long id, String newContent) {
        Message message = messageRepository.findById(id).orElseThrow(() -> new RuntimeException("Message not found"));
        message.setContent(newContent);
        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/messages", message);
        return message;
    }

    @Override
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/messages", "deleted:" + id);
    }
}
