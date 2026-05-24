package com.nyayhelp.aiservice.service;

import com.nyayhelp.aiservice.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are "NyayHelp Assistant", an in-app helper for an Indian legal-aid platform that connects victims (CLIENTs) with lawyers.

            Your job is to help the viewer with:
              1. General, non-binding legal information relevant to Indian law (IPC, CrPC, consumer, family, labour, etc.). Always add: "This is general information, not legal advice."
              2. Explaining platform features: posting a case, browsing lawyers (/lawyers), the IPC Database (/ipc), Legal Articles (/article), the Client Dashboard, the Lawyer Dashboard, and the secure case-chat (/chat/:caseId).
              3. Helping the viewer connect with the right counterparty: a CLIENT looking for a lawyer should be guided to "Post Case" or "Find a Lawyer"; a LAWYER should be guided to their Dashboard to view applied/assigned cases.
              4. Onboarding and navigation help.

            Viewer role for this conversation: %s.

            Rules:
              - Be concise (under 150 words unless detail is requested).
              - Never claim to be a substitute for a qualified advocate.
              - Never ask for or store sensitive info (OTPs, banking details, Aadhaar numbers).
              - If the user asks something outside Indian legal aid / platform help, politely redirect them.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiService(RestClient restClient,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.model}") String model,
                         @Value("${gemini.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String generateReply(List<ChatMessage> history, String viewerRole) {
        if (!isConfigured()) {
            return "The AI assistant is not configured yet. Please ask the administrator to set the GEMINI_API_KEY.";
        }

        String role = (viewerRole == null || viewerRole.isBlank()) ? "GUEST" : viewerRole.toUpperCase();
        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(role);

        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : history) {
            if (m == null || m.getContent() == null || m.getContent().isBlank()) continue;
            String r = "assistant".equalsIgnoreCase(m.getRole()) ? "model" : "user";
            Map<String, Object> part = Map.of("text", m.getContent());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("role", r);
            entry.put("parts", List.of(part));
            contents.add(entry);
        }

        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("system_instruction", systemInstruction);
        body.put("contents", contents);
        body.put("generationConfig", Map.of(
                "temperature", 0.4,
                "maxOutputTokens", 512
        ));

        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);
        } catch (HttpStatusCodeException e) {
            log.error("Gemini HTTP {} -> {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI error " + e.getStatusCode().value() + ": " + truncate(e.getResponseBodyAsString(), 300);
        } catch (Exception e) {
            log.error("Gemini call failed", e);
            return "AI error: " + e.getClass().getSimpleName() + " - " + truncate(String.valueOf(e.getMessage()), 200);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) return "No response from AI.";
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) return "No response from AI.";
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) return "No response from AI.";
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) return "No response from AI.";
        Object text = parts.get(0).get("text");
        return text == null ? "No response from AI." : text.toString().trim();
    }
}
