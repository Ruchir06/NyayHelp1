package com.nyayhelp.chatservice.controller;

import com.nyayhelp.chatservice.dto.ChatResponse;
import com.nyayhelp.chatservice.dto.MessageRequest;
import com.nyayhelp.chatservice.model.Message;
import com.nyayhelp.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService service;

    // 🔥 SEND MESSAGE
    @PostMapping("/send")
    public String send(@RequestBody MessageRequest request) {
        return service.sendMessage(request);
    }

    // 🔥 GET CHAT
    @GetMapping("/{caseId}")
   public List<ChatResponse> get(@PathVariable Long caseId) {
    return service.getMessages(caseId);
}

    
}