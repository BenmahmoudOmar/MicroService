package com.esprit.internify.controllers;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody Conversation conversation) {
        return ResponseEntity.ok(conversationService.createConversation(conversation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Long id) {
        return conversationService.getConversationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getMessagesByConversation(id));
    }

    @GetMapping("/user/{userId}")
    public List<Conversation> getUserConversations(@PathVariable Long userId) {
        return conversationService.getUserConversationsSortedByLastMessage(userId);
    }

    @PutMapping("/{id}/favorite/{userId}")
    public ResponseEntity<Conversation> toggleFavorite(@PathVariable Long id, @PathVariable Long userId) {
        Conversation updatedConversation = conversationService.toggleFavorite(id, userId);
        return ResponseEntity.ok(updatedConversation);
    }

    @PutMapping("/{id}/mute/{userId}")
    public ResponseEntity<Conversation> toggleMute(@PathVariable Long id, @PathVariable Long userId) {
        Conversation updatedConversation = conversationService.toggleMute(id, userId);
        return ResponseEntity.ok(updatedConversation);
    }

    @GetMapping("/{conversationId}/search")
    public List<Message> searchMessagesInConversation(
            @PathVariable Long conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String sentDate) {

        List<Message> messages = conversationService.searchMessagesInConversation(conversationId, content, sentDate);

        return messages;
    }
}
