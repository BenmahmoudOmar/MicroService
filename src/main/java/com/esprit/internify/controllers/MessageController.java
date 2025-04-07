package com.esprit.internify.controllers;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.services.ConversationService;
import com.esprit.internify.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@Tag(name = "gestion_message")
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ConversationService conversationService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.sendMessage(message));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody String newContent) {
        return ResponseEntity.ok(messageService.updateMessage(id, newContent));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping("/{messageId}/status")
    public ResponseEntity<Message> updateMessageStatusToRead(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.updateMessageStatusToRead(messageId));
    }

    @PostMapping("/send-with-attachment")
    public ResponseEntity<Message> sendMessageWithAttachment(
            @RequestParam("message") String messageContent,
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") Long conversationId) {

        try {
            // Convert the JSON string to a Message object
            ObjectMapper objectMapper = new ObjectMapper();
            Message message = objectMapper.readValue(messageContent, Message.class);

            // Retrieve the conversation by ID
            Conversation conversation = conversationService.getConversationById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Call the service method to send the message with the attachment
            Message savedMessage = messageService.sendMessageWithAttachment(message, file, conversation);

            return ResponseEntity.ok(savedMessage);
        } catch (IOException e) {
            logger.error("Error processing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/upload/{fileName:.+}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String fileName) {
        Resource resource = messageService.downloadImage(fileName);

        if (resource == null) {
            System.out.println("File not found: " + fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Set the content type based on the file extension
        String contentType = "application/octet-stream"; // Default to binary
        if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        }

        // Set the content type and headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<Void> pinMessage(@PathVariable Long id) {
        messageService.togglePinMessage(id);
        return ResponseEntity.ok().build(); // Return a 200 OK response
    }

    @GetMapping("/user/{userId}/message-stats")
    public ResponseEntity<Map<String, Object>> getMessageStats(@PathVariable Long userId) {
        Map<String, Object> messageStats = messageService.getTotalMessagesAndTypesByUser(userId);
        return ResponseEntity.ok(messageStats);
    }

    @GetMapping("/user/{userId}/sent-message-stats")
    public ResponseEntity<Map<String, Object>> getSentMessageStats(@PathVariable Long userId) {
        Map<String, Object> messageStats = messageService.getTotalSentMessagesAndTypesByUser(userId);
        return ResponseEntity.ok(messageStats);
    }

    @GetMapping("/user/{userId}/received-message-stats")
    public ResponseEntity<Map<String, Object>> getReceivedMessageStats(@PathVariable Long userId) {
        Map<String, Object> messageStats = messageService.getTotalReceivedMessagesAndTypesByUser(userId);
        return ResponseEntity.ok(messageStats);
    }

    @GetMapping("/message-stats")
    public ResponseEntity<Map<String, Object>> getMessageStats() {
        Map<String, Object> messageStats = messageService.getTotalMessagesAndTypes();
        return ResponseEntity.ok(messageStats);
    }
}
