package com.nyayhelp.userservice.controller;

import com.nyayhelp.userservice.dto.UserProfileRequest;
import com.nyayhelp.userservice.model.UserProfile;
import com.nyayhelp.userservice.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/by-auth/{authId}")
    public UserProfile getByAuthId(@PathVariable Long authId) {
        return service.getByAuthId(authId);
    }
}