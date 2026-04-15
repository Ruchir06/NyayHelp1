package com.nyayhelp.userservice.service;

import com.nyayhelp.userservice.dto.UserProfileRequest;
import com.nyayhelp.userservice.model.UserProfile;
import com.nyayhelp.userservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    public String createProfile(UserProfileRequest request) {

        UserProfile user = new UserProfile();

        user.setAuthUserId(request.authUserId);
        user.setRole(request.role);

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
    return repository.findAll()
            .stream()
            .filter(u -> authId.equals(u.getAuthUserId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User not found"));
}
}