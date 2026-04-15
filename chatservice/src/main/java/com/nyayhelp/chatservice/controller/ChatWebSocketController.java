package com.nyayhelp.chatservice.controller;

import com.nyayhelp.chatservice.dto.MessageRequest;
import com.nyayhelp.chatservice.model.Message;
import com.nyayhelp.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService service;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void send(MessageRequest request) {

        // 🔥 Save message (reuse existing logic)
        service.sendMessage(request);

        // 🔥 Send to subscribers
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.caseId,
                request
        );
    }
}