package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IMessageService {
    Message sendMessage(Message message);
    Optional<Message> getMessageById(Long messageId);
    Message updateMessage(Long messageId, String newContent);
    void deleteMessage(Long messageId);
    Message updateMessageStatusToRead(Long messageId);
    String uploadImage(MultipartFile file) throws IOException;
    String uploadPdf(MultipartFile file) throws IOException;
    String uploadAudio(MultipartFile file) throws IOException;
    Message sendMessageWithAttachment(Message message, MultipartFile file, Conversation conversation) throws IOException;
    Resource downloadImage(String fileName);
    void togglePinMessage(Long messageId);
    Map<String, Object> getTotalMessagesAndTypesByUser (Long userId);
    Map<String, Object> getTotalSentMessagesAndTypesByUser (Long userId);
    Map<String, Object> getTotalReceivedMessagesAndTypesByUser (Long userId);
    Map<String, Object> getTotalMessagesAndTypes();
}
