package com.esprit.internify.controllers;

import com.esprit.internify.entities.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageWebsocketController {
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message broadcastMessage(Message message) {
        return message;
    }
}
