package com.nyayhelp.caseservice.service;

import com.nyayhelp.caseservice.dto.CaseApplicationResponse;
import com.nyayhelp.caseservice.dto.CaseRequest;
import com.nyayhelp.caseservice.dto.UserProfileResponse;
import com.nyayhelp.caseservice.model.Case;
import com.nyayhelp.caseservice.model.CaseApplication;
import com.nyayhelp.caseservice.repository.CaseApplicationRepository;
import com.nyayhelp.caseservice.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    
    // 🔹 CREATE CASE (CLIENT)
    public String createCase(CaseRequest request) {

        Case c = new Case();

        c.setClientId(request.clientId);
        c.setTitle(request.title);
        c.setDescription(request.description);
        c.setCategory(request.category);
        c.setLocation(request.location);

        c.setStatus("OPEN");

        repository.save(c);

        return "Case Created Successfully";
    }


    public String applyForCase(Long caseId, Long lawyerId) {

    CaseApplication app = new CaseApplication();
    app.setCaseId(caseId);
    app.setLawyerId(lawyerId);
    app.setStatus("APPLIED");

    applicationRepo.save(app);

    return "Applied successfully";
    }



    // 🔹 GET CASES FOR LAWYER (MATCHING)
    public List<Case> getCasesForLawyer(String category, String location) {

        return repository.findByStatusAndCategoryAndLocation("OPEN", category, location);
    }

    // 🔹 ACCEPT CASE
    public String acceptCase(Long caseId, Long lawyerId) {

        Case c = repository.findById(caseId).orElseThrow();

        c.setStatus("ACCEPTED");
        c.setLawyerId(lawyerId);

        repository.save(c);

        return "Case Accepted";
    }

    // 🔹 REJECT CASE
    public String rejectCase(Long caseId) {

        Case c = repository.findById(caseId).orElseThrow();

        c.setStatus("REJECTED");

        repository.save(c);

        return "Case Rejected";
    }
    //Lawyer Dashboard
    public List<Case> getAcceptedCases(Long lawyerId) {
    return repository.findAll()
            .stream()
            .filter(c -> "ACCEPTED".equals(c.getStatus()) && lawyerId.equals(c.getLawyerId()))
            .toList();
} 

    //Client Dashboard

public List<Case> getClientCases(Long clientId) {
    return repository.findAll()
            .stream()
            .filter(c -> clientId.equals(c.getClientId()))
            .toList();
} 

//GET APPLICATIONS (CLIENT VIEW)

public List<CaseApplicationResponse> getApplications(Long caseId) {

    List<CaseApplication> apps = applicationRepo.findByCaseId(caseId);

    return apps.stream().map(app -> {

        // ✅ DIRECT DTO MAPPING (NO CAST)
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
    


//Select Lawyer

public String selectLawyer(Long caseId, Long lawyerId) {

    Case c = repository.findById(caseId).orElseThrow();

    c.setStatus("ASSIGNED");
    c.setLawyerId(lawyerId);

    repository.save(c);

    // update applications
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

}