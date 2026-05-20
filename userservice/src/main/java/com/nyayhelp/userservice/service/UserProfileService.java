package com.nyayhelp.userservice.service;

import com.nyayhelp.userservice.dto.UserProfileRequest;
import com.nyayhelp.userservice.model.UserProfile;
import com.nyayhelp.userservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
}