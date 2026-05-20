package com.nyayhelp.caseservice.controller;

import com.nyayhelp.caseservice.dto.CaseApplicationResponse;
import com.nyayhelp.caseservice.dto.CaseRequest;
import com.nyayhelp.caseservice.model.Case;
import com.nyayhelp.caseservice.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseService service;

    @PostMapping("/create")
    public String create(@RequestBody CaseRequest request, Authentication authentication) {
        return service.createCase(request, authentication);
    }

    @GetMapping("/lawyer")
    public List<Case> getCases(@RequestParam String category,
                               @RequestParam String location) {
        return service.getCasesForLawyer(category, location);
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable Long id, Authentication authentication) {
        return service.applyForCase(id, authentication);
    }

    @GetMapping("/lawyer/accepted")
    public List<Case> accepted(Authentication authentication) {
        return service.getAcceptedCases(authentication);
    }

    @GetMapping("/client")
    public List<Case> clientCases(Authentication authentication) {
        return service.getClientCases(authentication);
    }

    @GetMapping("/{id}/applications")
    public List<CaseApplicationResponse> applications(@PathVariable Long id) {
        return service.getApplications(id);
    }

    @PutMapping("/{id}/select")
    public String select(@PathVariable Long id,
                         @RequestParam Long lawyerId,
                         Authentication authentication) {
        return service.selectLawyer(id, lawyerId, authentication);
    }

    @GetMapping("/{id}")
    public Case getCase(@PathVariable Long id) {
        return service.getById(id);
    }
}