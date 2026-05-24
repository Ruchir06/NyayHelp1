package com.nyayhelp.userservice.controller;

import com.nyayhelp.userservice.dto.InternalProfileCreateRequest;
import com.nyayhelp.userservice.dto.LawyerVerificationRequest;
import com.nyayhelp.userservice.dto.UserProfileRequest;
import com.nyayhelp.userservice.model.UserProfile;
import com.nyayhelp.userservice.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserProfileService service;

    @PostMapping("/create")
    public String create(@RequestBody UserProfileRequest request, Authentication authentication) {
        return service.createProfile(request, authentication);
    }

    @PostMapping("/internal/create")
    public String internalCreate(@RequestBody InternalProfileCreateRequest request) {
        return service.createProfileInternal(request);
    }

    @GetMapping("/by-auth/{authId}")
    public UserProfile getByAuthId(@PathVariable Long authId) {
        return service.getByAuthId(authId);
    }

    @GetMapping("/me")
    public UserProfile me(Authentication authentication) {
        return service.me(authentication);
    }

    @PostMapping("/verification")
    public String submitVerification(@RequestBody LawyerVerificationRequest request,
                                     Authentication authentication) {
        return service.submitVerification(request, authentication);
    }

    @GetMapping("/admin/pending")
    public List<UserProfile> adminPending() {
        return service.listPendingLawyers();
    }

    @PostMapping("/admin/{authUserId}/approve")
    public String adminApprove(@PathVariable Long authUserId) {
        return service.approveLawyer(authUserId);
    }

    @PostMapping("/admin/{authUserId}/reject")
    public String adminReject(@PathVariable Long authUserId,
                              @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return service.rejectLawyer(authUserId, reason);
    }
}