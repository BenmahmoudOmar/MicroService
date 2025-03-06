package com.esprit.internify.services;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.entities.MessageStatus;
import com.esprit.internify.entities.MessageType;
import com.esprit.internify.repository.MessageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // Return the upload directory path
    @Getter
    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public Message sendMessage(Message message) {
        message.setId(null);
        message.setTimestamp(LocalDateTime.now());
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
        message.setStatus(MessageStatus.SENT);
        message.setTimestamp(LocalDateTime.now());
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

    @Override
    public Message updateMessageStatusToRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.READ);
        message.setReadAt(LocalDateTime.now()); // Set the read timestamp
        Message updatedMessage = messageRepository.save(message);

        // Emit the updated message status
        messagingTemplate.convertAndSend("/topic/messages", message);
        return updatedMessage;
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the specified directory
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // Return the URL of the uploaded image
        return "/messages/upload/" + fileName; // Adjust the URL as needed
    }

    @Override
    public String uploadPdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the specified directory
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // Return the URL of the uploaded PDF
        return "/messages/upload/" + fileName; // Adjust the URL as needed
    }

    public String uploadAudio(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the specified directory
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // Return the URL of the uploaded audio
        return "/messages/upload/" + fileName; // Adjust the URL as needed
    }

    @Override
    public Message sendMessageWithAttachment(Message message, MultipartFile file, Conversation conversation) throws IOException {
        String attachmentUrl;

        // Check the file type
        if (file != null && !file.isEmpty()) {
            String fileType = file.getContentType();
            if (fileType.startsWith("image/")) {
                // Upload the image and get the URL
                attachmentUrl = uploadImage(file);
                message.setMessageType(MessageType.IMAGE); // Set message type to IMAGE
            } else if (fileType.equals("application/pdf")) {
                // Upload the PDF and get the URL
                attachmentUrl = uploadPdf(file);
                message.setMessageType(MessageType.PDF); // Set message type to PDF
            }else if (fileType.startsWith("audio/")) { // Check for audio files
                attachmentUrl = uploadAudio(file); // New method for audio upload
                message.setMessageType(MessageType.AUDIO); // Set message type to AUDIO
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }
        } else {
            attachmentUrl = null; // No file uploaded
            message.setMessageType(MessageType.TEXT); // Set message type to TEXT if no file
        }

        // Set the attachment URL in the message
        message.setAttachmentUrl(attachmentUrl);
        message.setTimestamp(LocalDateTime.now());
        message.setConversation(conversation);

        // Save the message
        message.setId(null); // Ensure the ID is null for new messages
        Message savedMessage = messageRepository.save(message);

        // Emit the created message
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
        return savedMessage;
    }

    public Resource downloadImage(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            System.out.println("🔍 Recherche du fichier : " + filePath.toString()); // DEBUG

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                System.out.println("✅ Fichier trouvé : " + filePath.toString());
                return resource;
            } else {
                System.out.println("❌ Fichier introuvable ou illisible : " + filePath.toString());
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement : " + e.getMessage());
            return null;
        }
    }

    @Override
    public void togglePinMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsPinned(!message.getIsPinned()); // Toggle the isPinned status
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
            totalMessages += (Long) result[0]; // Accumulate total messages
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]); // Map message type to count
        }

        resultMap.put("TOTAL", totalMessages); // Add total messages to the map
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalSentMessagesAndTypesByUser (Long userId) {
        List<Object[]> results = messageRepository.countTotalSentMessagesAndTypesByUser (userId);
        Map<String, Object> resultMap = new HashMap<>();
        long totalSentMessages = 0;

        for (Object[] result : results) {
            totalSentMessages += (Long) result[0]; // Accumulate total sent messages
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]); // Map message type to count
        }

        resultMap.put("TOTAL", totalSentMessages); // Add total sent messages to the map
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalReceivedMessagesAndTypesByUser (Long userId) {
        List<Object[]> results = messageRepository.countTotalReceivedMessagesAndTypesByUser (userId);
        Map<String, Object> resultMap = new HashMap<>();
        long totalReceivedMessages = 0;

        for (Object[] result : results) {
            totalReceivedMessages += (Long) result[0]; // Accumulate total received messages
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]); // Map message type to count
        }

        resultMap.put("TOTAL", totalReceivedMessages); // Add total received messages to the map
        return resultMap;
    }

    @Override
    public Map<String, Object> getTotalMessagesAndTypes() {
        List<Object[]> results = messageRepository.countTotalMessagesAndTypes();
        Map<String, Object> resultMap = new HashMap<>();
        long totalMessages = 0;

        for (Object[] result : results) {
            totalMessages += (Long) result[0]; // Accumulate total messages
            resultMap.put(((MessageType) result[1]).name(), (Long) result[0]); // Map message type to count
        }

        resultMap.put("TOTAL", totalMessages); // Add total received messages to the map
        return resultMap;
    }
}