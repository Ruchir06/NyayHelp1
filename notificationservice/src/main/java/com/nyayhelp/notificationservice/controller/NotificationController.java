package com.nyayhelp.notificationservice.controller;

import com.nyayhelp.notificationservice.dto.EmailRequest;
import com.nyayhelp.notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/health")
    public String health() {
        return "notification-service up";
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody EmailRequest req) {
        boolean delivered = emailService.send(req.to, req.event, req.data);
        return Map.of("queued", true, "delivered", delivered);
    }
}
