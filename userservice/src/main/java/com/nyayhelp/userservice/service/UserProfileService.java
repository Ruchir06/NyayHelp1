package com.nyayhelp.userservice.service;

import com.nyayhelp.userservice.dto.InternalProfileCreateRequest;
import com.nyayhelp.userservice.dto.LawyerVerificationRequest;
import com.nyayhelp.userservice.dto.UserProfileRequest;
import com.nyayhelp.userservice.model.UserProfile;
import com.nyayhelp.userservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    public String createProfile(UserProfileRequest request, Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        Long authUserId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        if (repository.findByAuthUserId(authUserId).isPresent()) {
            throw new RuntimeException("Profile already exists");
        }

        UserProfile user = new UserProfile();
        user.setAuthUserId(authUserId);
        user.setEmail(email);
        user.setRole(role);
        user.setName(request.name);
        user.setLocation(request.location);
        user.setCategory(request.category);
        user.setExperience(request.experience);
        user.setFees(request.fees);
        user.setVerificationStatus("PENDING");

        repository.save(user);

        return "Profile Created";
    }

    public UserProfile getByAuthId(Long authId) {
        return repository.findByAuthUserId(authId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String createProfileInternal(InternalProfileCreateRequest request) {
        if (request.authUserId == null) {
            throw new RuntimeException("authUserId is required");
        }
        if (repository.findByAuthUserId(request.authUserId).isPresent()) {
            return "Profile already exists";
        }

        UserProfile user = new UserProfile();
        user.setAuthUserId(request.authUserId);
        user.setEmail(request.email);
        user.setRole(request.role);
        user.setName(request.name);
        user.setLocation(request.location);
        user.setCategory(request.category);
        user.setExperience(request.experience);
        user.setFees(request.fees);
        user.setVerificationStatus("LAWYER".equalsIgnoreCase(request.role) ? "NOT_SUBMITTED" : "VERIFIED");

        repository.save(user);
        return "Profile Created";
    }

    public String submitVerification(LawyerVerificationRequest request, Authentication authentication) {
        Long authUserId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        if (!"LAWYER".equals(role)) {
            throw new RuntimeException("Only lawyers can submit verification.");
        }
        if (request == null
                || isBlank(request.livePhotoBase64)
                || isBlank(request.aadhaarBase64)
                || isBlank(request.barCouncilBase64)
                || isBlank(request.licenseBase64)) {
            throw new RuntimeException("All four documents are required.");
        }

        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setLivePhotoBase64(request.livePhotoBase64);
        profile.setAadhaarBase64(request.aadhaarBase64);
        profile.setBarCouncilBase64(request.barCouncilBase64);
        profile.setLicenseBase64(request.licenseBase64);
        if (!isBlank(request.barCouncilId)) {
            profile.setBarCouncilId(request.barCouncilId);
        }
        profile.setVerificationStatus("PENDING");
        profile.setRejectionReason(null);
        repository.save(profile);
        return "Verification submitted";
    }

    public UserProfile me(Authentication authentication) {
        Long authUserId = (Long) authentication.getDetails();
        return repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public List<UserProfile> listPendingLawyers() {
        return repository.findByRoleAndVerificationStatus("LAWYER", "PENDING");
    }

    public String approveLawyer(Long authUserId) {
        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (!"LAWYER".equalsIgnoreCase(profile.getRole())) {
            throw new RuntimeException("Target is not a lawyer.");
        }
        profile.setVerificationStatus("APPROVED");
        profile.setRejectionReason(null);
        repository.save(profile);
        return "Approved";
    }

    public String rejectLawyer(Long authUserId, String reason) {
        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (!"LAWYER".equalsIgnoreCase(profile.getRole())) {
            throw new RuntimeException("Target is not a lawyer.");
        }
        profile.setVerificationStatus("REJECTED");
        profile.setRejectionReason(reason);
        repository.save(profile);
        return "Rejected";
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}