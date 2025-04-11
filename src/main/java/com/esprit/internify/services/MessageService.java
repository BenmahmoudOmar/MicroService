package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.entities.MessageStatus;
import com.esprit.internify.entities.MessageType;
import com.esprit.internify.repository.ConversationRepository;
import com.esprit.internify.repository.MessageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Getter
    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public Message sendMessage(Message message) {
        message.setId(null);
        message.setTimestamp(LocalDateTime.now());
        Conversation conversation = message.getConversation();
        if (conversation != null) {
            Conversation managedConversation = conversationRepository.findById(conversation.getId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            managedConversation.setLastMessageTimestamp(LocalDateTime.now());

            managedConversation.getMessages().add(message);
            message.setConversation(managedConversation);
        }
        Message savedMessage = messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/conversations", conversation);
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
        messagingTemplate.convertAndSend("/topic/notifications/" + savedMessage.getReceiver().getId(), savedMessage);

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
        message.setStatus(MessageStatus.SENT);
        message.setTimestamp(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

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
        message.setContent("Deleted message");
        Message deletedMessage = messageRepository.save(message);

        Map<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("action", "deleted");
        deleteMessage.put("message", deletedMessage);
        messagingTemplate.convertAndSend("/topic/messages", deleteMessage);
    }

    @Override
    public Message updateMessageStatusToRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.READ);
        message.setReadAt(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/messages", message);
        return updatedMessage;
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        return "/messages/upload/" + fileName;
    }

    @Override
    public String uploadPdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        return "/messages/upload/" + fileName;
    }

    public String uploadAudio(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        return "/messages/upload/" + fileName;
    }

    @Override
    public Message sendMessageWithAttachment(Message message, MultipartFile file, Conversation conversation) throws IOException {
        String attachmentUrl;

        if (file != null && !file.isEmpty()) {
            String fileType = file.getContentType();
            if (fileType.startsWith("image/")) {
                attachmentUrl = uploadImage(file);
                message.setMessageType(MessageType.IMAGE);
            } else if (fileType.equals("application/pdf")) {
                attachmentUrl = uploadPdf(file);
                message.setMessageType(MessageType.PDF);
            }else if (fileType.startsWith("audio/")) {
                attachmentUrl = uploadAudio(file);
                message.setMessageType(MessageType.AUDIO);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }
        } else {
            attachmentUrl = null;
            message.setMessageType(MessageType.TEXT);
        }

        message.setAttachmentUrl(attachmentUrl);
        message.setTimestamp(LocalDateTime.now());
        Conversation managedConversation = conversationRepository.findById(conversation.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        managedConversation.setLastMessageTimestamp(LocalDateTime.now());
        managedConversation.getMessages().add(message);
        message.setConversation(managedConversation);
        message.setId(null);
        Message savedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/conversations", conversation);
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
        messagingTemplate.convertAndSend("/topic/notifications/" + savedMessage.getReceiver().getId(), savedMessage);
        return savedMessage;
    }

    public Resource downloadImage(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            System.out.println("🔍 Recherche du fichier : " + filePath.toString());

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                System.out.println("Fichier trouvé : " + filePath.toString());
                return resource;
            } else {
                System.out.println("Fichier introuvable ou illisible : " + filePath.toString());
                return null;
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement : " + e.getMessage());
            return null;
        }
    }

    @Override
    public void togglePinMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsPinned(!message.getIsPinned());
        Message pinnedMessage = messageRepository.save(message);

        Map<String, Object> pinMessage = new HashMap<>();
        pinMessage.put("action", message.getIsPinned()?"pinned":"unpinned");
        pinMessage.put("message", pinnedMessage);
        messagingTemplate.convertAndSend("/topic/messages", pinMessage);
    }

    @Override
    public Map<String, Object> getTotalMessagesAndTypesByUser (Long userId) {
        List<Object[]> results = messageRepository.countTotalMessagesAndTypesByUser (userId);
        Map<String, Object> resultMap = new HashMap<>();
        long totalMessages = 0;

        for (Object[] result : results) {
            totalMessages += (Long) result[0];
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]);
        }

        resultMap.put("TOTAL", totalMessages);
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalSentMessagesAndTypesByUser (Long userId) {
        List<Object[]> results = messageRepository.countTotalSentMessagesAndTypesByUser (userId);
        Map<String, Object> resultMap = new HashMap<>();
        long totalSentMessages = 0;

        for (Object[] result : results) {
            totalSentMessages += (Long) result[0];
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]);
        }

        resultMap.put("TOTAL", totalSentMessages);
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalReceivedMessagesAndTypesByUser (Long userId) {
        List<Object[]> results = messageRepository.countTotalReceivedMessagesAndTypesByUser (userId);
        Map<String, Object> resultMap = new HashMap<>();
        long totalReceivedMessages = 0;

        for (Object[] result : results) {
            totalReceivedMessages += (Long) result[0];
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]);
        }

        resultMap.put("TOTAL", totalReceivedMessages);
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalMessagesAndTypes() {
        List<Object[]> results = messageRepository.countTotalMessagesAndTypes();
        Map<String, Object> resultMap = new HashMap<>();
        long totalMessages = 0;

        for (Object[] result : results) {
            totalMessages += (Long) result[0];
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]);
        }

        resultMap.put("TOTAL", totalMessages);
        return resultMap;
    }
}