package com.nyayhelp.caseservice.service;

import com.nyayhelp.caseservice.dto.CaseApplicationResponse;
import com.nyayhelp.caseservice.dto.CaseRequest;
import com.nyayhelp.caseservice.dto.UserProfileResponse;
import com.nyayhelp.caseservice.model.Case;
import com.nyayhelp.caseservice.model.CaseApplication;
import com.nyayhelp.caseservice.repository.CaseApplicationRepository;
import com.nyayhelp.caseservice.repository.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    @Autowired
    private CaseRepository repository;

    @Autowired
    private CaseApplicationRepository applicationRepo;

    @Autowired
    private RestClient restClient;

    @Value("${nyayhelp.userservice.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${nyayhelp.notificationservice.url:http://localhost:8086}")
    private String notificationServiceUrl;

    public String createCase(CaseRequest request, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Long clientId = (Long) authentication.getDetails();

        if (!"CLIENT".equals(role)) {
            throw new RuntimeException("Only client can create case");
        }

        Case c = new Case();
        c.setClientId(clientId);
        c.setTitle(request.title);
        c.setDescription(request.description);
        c.setCategory(request.category);
        c.setLocation(request.location);
        c.setStatus("OPEN");

        repository.save(c);

        UserProfileResponse client = fetchProfile(clientId);
        if (client != null) {
            sendEmail(client.email, "CASE_POSTED", Map.of(
                    "name", nullToEmpty(client.name),
                    "title", nullToEmpty(c.getTitle())
            ));
        }

        return "Case Created Successfully";
    }

    public List<Case> getCasesForLawyer(String category, String location) {
        return repository.findByStatusAndCategoryAndLocation("OPEN", category, location);
    }

    public String applyForCase(Long caseId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Long lawyerId = (Long) authentication.getDetails();

        if (!"LAWYER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only lawyer can apply");
        }

        UserProfileResponse lawyer = fetchProfile(lawyerId);
        if (lawyer == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your profile is under verification");
        }
        if (!"APPROVED".equalsIgnoreCase(lawyer.verificationStatus)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your profile is under verification");
        }

        Case targetCase = repository.findById(caseId).orElse(null);

        CaseApplication app = new CaseApplication();
        app.setCaseId(caseId);
        app.setLawyerId(lawyerId);
        app.setStatus("APPLIED");
        applicationRepo.save(app);

        sendEmail(lawyer.email, "BID_PLACED", Map.of(
                "name", nullToEmpty(lawyer.name),
                "caseTitle", targetCase != null ? nullToEmpty(targetCase.getTitle()) : "the case"
        ));

        return "Applied successfully";
    }

    public List<Case> getAcceptedCases(Authentication authentication) {
        Long lawyerId = (Long) authentication.getDetails();

        return repository.findAll()
                .stream()
                .filter(c -> "ASSIGNED".equals(c.getStatus()) && lawyerId.equals(c.getLawyerId()))
                .toList();
    }

    public List<Case> getClientCases(Authentication authentication) {
        Long clientId = (Long) authentication.getDetails();

        return repository.findAll()
                .stream()
                .filter(c -> clientId.equals(c.getClientId()))
                .toList();
    }

    public List<CaseApplicationResponse> getApplications(Long caseId) {
        List<CaseApplication> apps = applicationRepo.findByCaseId(caseId);

        return apps.stream().map(app -> {
            UserProfileResponse user = fetchProfile(app.getLawyerId());

            CaseApplicationResponse res = new CaseApplicationResponse();
            res.lawyerId = app.getLawyerId();
            res.status = app.getStatus();

            if (user != null) {
                res.name = user.name;
                res.category = user.category;
                res.experience = user.experience;
                res.fees = user.fees;
            }

            return res;
        }).toList();
    }

    public String selectLawyer(Long caseId, Long lawyerId, Authentication authentication) {
        Long currentClientId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        if (!"CLIENT".equals(role)) {
            throw new RuntimeException("Only client can select lawyer");
        }

        Case c = repository.findById(caseId).orElseThrow();

        if (!currentClientId.equals(c.getClientId())) {
            throw new RuntimeException("You can select lawyer only for your own case");
        }

        c.setStatus("ASSIGNED");
        c.setLawyerId(lawyerId);
        repository.save(c);

        List<CaseApplication> apps = applicationRepo.findByCaseId(caseId);

        for (CaseApplication app : apps) {
            if (app.getLawyerId().equals(lawyerId)) {
                app.setStatus("SELECTED");
            } else {
                app.setStatus("REJECTED");
            }
        }

        applicationRepo.saveAll(apps);

        UserProfileResponse client = fetchProfile(currentClientId);
        UserProfileResponse lawyer = fetchProfile(lawyerId);

        if (lawyer != null) {
            sendEmail(lawyer.email, "BID_ACCEPTED_LAWYER", Map.of(
                    "name", nullToEmpty(lawyer.name),
                    "caseTitle", nullToEmpty(c.getTitle())
            ));
        }
        if (client != null) {
            sendEmail(client.email, "BID_ACCEPTED_CLIENT", Map.of(
                    "name", nullToEmpty(client.name),
                    "caseTitle", nullToEmpty(c.getTitle()),
                    "lawyerName", lawyer != null ? nullToEmpty(lawyer.name) : "the advocate"
            ));
        }

        return "Lawyer selected successfully";
    }

    public Case getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
    }

    public String endChat(Long caseId, Authentication authentication) {
        Long currentClientId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        if (!"CLIENT".equals(role)) {
            throw new RuntimeException("Only the client can end the chat.");
        }

        Case c = repository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!currentClientId.equals(c.getClientId())) {
            throw new RuntimeException("You can only end chat for your own case.");
        }
        if (!"ASSIGNED".equals(c.getStatus())) {
            throw new RuntimeException("Chat can only be ended on an assigned case.");
        }
        if (Boolean.TRUE.equals(c.getChatEnded())) {
            return "Chat already ended.";
        }

        c.setChatEnded(true);
        repository.save(c);
        return "Chat ended.";
    }

    private UserProfileResponse fetchProfile(Long authUserId) {
        if (authUserId == null) return null;
        try {
            return restClient.get()
                    .uri(userServiceUrl + "/api/users/by-auth/" + authUserId)
                    .retrieve()
                    .body(UserProfileResponse.class);
        } catch (Exception e) {
            log.warn("Failed to fetch profile for authUserId={}: {}", authUserId, e.getMessage());
            return null;
        }
    }

    private void sendEmail(String to, String event, Map<String, Object> data) {
        if (to == null || to.isBlank()) return;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("to", to);
            body.put("event", event);
            body.put("data", data);
            restClient.post()
                    .uri(notificationServiceUrl + "/api/notifications/send")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification dispatch failed (event={}, to={}): {}", event, to, e.getMessage());
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
