package com.nyayhelp.caseservice.controller;

import com.nyayhelp.caseservice.dto.CaseApplicationResponse;
import com.nyayhelp.caseservice.dto.CaseRequest;
import com.nyayhelp.caseservice.model.Case;
import com.nyayhelp.caseservice.model.CaseApplication;
import com.nyayhelp.caseservice.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseService service;

    // 🔹 CLIENT
    @PostMapping("/create")
    public String create(@RequestBody CaseRequest request) {
        return service.createCase(request);
    }

    // 🔹 LAWYER (MATCHING)
    @GetMapping("/lawyer")
    public List<Case> getCases(
            @RequestParam String category,
            @RequestParam String location
    ) {
        return service.getCasesForLawyer(category, location);
    }

    // 🔹 ACCEPT
    @PutMapping("/{id}/accept")
    public String accept(@PathVariable Long id, @RequestParam Long lawyerId) {
        return service.acceptCase(id, lawyerId);
    }

    // 🔹 REJECT
    @PutMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        return service.rejectCase(id);
    }

    @GetMapping("/lawyer/accepted")
public List<Case> accepted(@RequestParam Long lawyerId) {
    return service.getAcceptedCases(lawyerId);
} 

@GetMapping("/client")
public List<Case> clientCases(@RequestParam Long clientId) {
    return service.getClientCases(clientId);
} 

// APPLY
@PostMapping("/{id}/apply")
public String apply(@PathVariable Long id, @RequestParam Long lawyerId) {
    return service.applyForCase(id, lawyerId);
}



// Get Applications
@GetMapping("/{id}/applications")
public List<CaseApplicationResponse> applications(@PathVariable Long id) {
    return service.getApplications(id);


}

//SELECT LAWYER 
@PutMapping("/{id}/select")
public String select(@PathVariable Long id, @RequestParam Long lawyerId) {
    return service.selectLawyer(id, lawyerId);
}

@GetMapping("/{id}")
public Case getCase(@PathVariable Long id) {
    return service.getById(id);
}

}