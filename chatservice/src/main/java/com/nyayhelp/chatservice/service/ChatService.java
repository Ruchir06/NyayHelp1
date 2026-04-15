package com.nyayhelp.chatservice.service;

import com.nyayhelp.chatservice.dto.CaseResponse;
import com.nyayhelp.chatservice.dto.MessageRequest;
import com.nyayhelp.chatservice.model.Message;
import com.nyayhelp.chatservice.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private MessageRepository repository;

    @Autowired
    private RestClient restClient;

    // 🔥 SEND MESSAGE
    public String sendMessage(MessageRequest request) {

         // 🔥 CALL CASE SERVICE
    CaseResponse caseData = restClient.get()
            .uri("http://localhost:8083/api/cases/" + request.caseId)
            .retrieve()
            .body(CaseResponse.class);

    // ❌ CASE NOT ASSIGNED
    if (caseData == null || !"ASSIGNED".equals(caseData.status)) {
        throw new RuntimeException("Chat not allowed. Case not assigned yet.");
    }

    // ❌ NOT CLIENT OR LAWYER
    if (!request.senderId.equals(caseData.clientId) &&
        !request.senderId.equals(caseData.lawyerId)) {
        throw new RuntimeException("Unauthorized to chat in this case.");
    }

        Message msg = new Message();

        msg.setCaseId(request.caseId);
        msg.setSenderId(request.senderId);
        msg.setSenderRole(request.senderRole);
        msg.setContent(request.content);
        msg.setTimestamp(LocalDateTime.now());

        repository.save(msg);

        return "Message Sent";
    }

    // 🔥 GET CHAT HISTORY
  
    public List<com.nyayhelp.chatservice.dto.ChatResponse> getMessages(Long caseId) {

    List<Message> messages = repository.findByCaseIdOrderByTimestampAsc(caseId);

    return messages.stream().map(msg -> {

        // 🔥 CALL USER SERVICE
        com.nyayhelp.chatservice.dto.UserProfileResponse user = restClient.get()
                .uri("http://localhost:8082/api/users/by-auth/" + msg.getSenderId())
                .retrieve()
                .body(com.nyayhelp.chatservice.dto.UserProfileResponse.class);

        com.nyayhelp.chatservice.dto.ChatResponse res =
                new com.nyayhelp.chatservice.dto.ChatResponse();

        if (user != null) {
            res.name = user.name;
            res.role = user.role;
        }

        res.message = msg.getContent();
        res.time = msg.getTimestamp().toString();

        return res;

    }).toList();
}
}