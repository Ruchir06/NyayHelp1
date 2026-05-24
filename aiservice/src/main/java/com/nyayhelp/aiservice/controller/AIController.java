package com.nyayhelp.aiservice.controller;

import com.nyayhelp.aiservice.dto.ChatRequest;
import com.nyayhelp.aiservice.dto.ChatResponse;
import com.nyayhelp.aiservice.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final GeminiService geminiService;

    public AIController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "configured", geminiService.isConfigured()
        ));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String reply = geminiService.generateReply(request.getMessages(), request.getViewerRole());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
