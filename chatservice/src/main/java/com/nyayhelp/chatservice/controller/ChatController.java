package com.nyayhelp.chatservice.controller;

import com.nyayhelp.chatservice.dto.ChatResponse;
import com.nyayhelp.chatservice.dto.MessageRequest;
import com.nyayhelp.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService service;

    @PostMapping("/send")
    public String send(@RequestBody MessageRequest request, Authentication authentication) {
        return service.sendMessage(request, authentication);
    }

    @GetMapping("/{caseId}")
    public List<ChatResponse> get(@PathVariable Long caseId) {
        return service.getMessages(caseId);
    }
}