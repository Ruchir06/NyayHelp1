package com.nyayhelp.aiservice.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ChatRequest {

    @NotEmpty
    private List<ChatMessage> messages;

    private String viewerRole;

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public String getViewerRole() { return viewerRole; }
    public void setViewerRole(String viewerRole) { this.viewerRole = viewerRole; }
}
