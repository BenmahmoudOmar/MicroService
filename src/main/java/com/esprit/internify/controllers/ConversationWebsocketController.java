package com.esprit.internify.controllers;

import com.esprit.internify.entities.Message;
import com.esprit.internify.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConversationWebsocketController {
    private final ConversationService conversationService;

    @MessageMapping("/getMessagesByConversation")
    @SendTo("/topic/conversations")
    public List<Message> sendMessages(Long conversationId) {
        return conversationService.getMessagesByConversation(conversationId);
    }
}
