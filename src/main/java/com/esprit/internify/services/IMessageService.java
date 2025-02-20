package com.esprit.internify.services;

import com.esprit.internify.entities.Message;
import java.util.List;
import java.util.Optional;

public interface IMessageService {
    Message sendMessage(Message message);
    Optional<Message> getMessageById(Long messageId);
    Message updateMessage(Long messageId, String newContent);
    void deleteMessage(Long messageId);
}
