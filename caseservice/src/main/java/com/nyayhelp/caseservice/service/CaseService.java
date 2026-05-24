package com.nyayhelp.caseservice.service;

import com.nyayhelp.caseservice.dto.CaseApplicationResponse;
import com.nyayhelp.caseservice.dto.CaseRequest;
import com.nyayhelp.caseservice.dto.UserProfileResponse;
import com.nyayhelp.caseservice.model.Case;
import com.nyayhelp.caseservice.model.CaseApplication;
import com.nyayhelp.caseservice.repository.CaseApplicationRepository;
import com.nyayhelp.caseservice.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CaseService {

    @Autowired
    private CaseRepository repository;

    @Autowired
    private CaseApplicationRepository applicationRepo;

    @Autowired
    private RestClient restClient;

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
        return "Case Created Successfully";
    }

    public List<Case> getCasesForLawyer(String category, String location) {
        return repository.findByStatusAndCategoryAndLocation("OPEN", category, location);
    }

    public String applyForCase(Long caseId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Long lawyerId = (Long) authentication.getDetails();

        if (!"LAWYER".equals(role)) {
            throw new RuntimeException("Only lawyer can apply");
        }

        CaseApplication app = new CaseApplication();
        app.setCaseId(caseId);
        app.setLawyerId(lawyerId);
        app.setStatus("APPLIED");

        applicationRepo.save(app);
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
            UserProfileResponse user = restClient.get()
                    .uri("http://localhost:8082/api/users/by-auth/" + app.getLawyerId())
                    .retrieve()
                    .body(UserProfileResponse.class);

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
}