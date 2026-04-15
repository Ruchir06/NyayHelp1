package com.nyayhelp.authservice.service;

import com.nyayhelp.authservice.dto.AuthResponse;
import com.nyayhelp.authservice.dto.LoginRequest;
import com.nyayhelp.authservice.dto.RegisterRequest;
import com.nyayhelp.authservice.dto.UserProfileRequest;
import com.nyayhelp.authservice.model.User;
import com.nyayhelp.authservice.repository.UserRepository;
import com.nyayhelp.authservice.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestClient restClient;

    public String register(RegisterRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());

        userRepository.save(user);

        UserProfileRequest profile = new UserProfileRequest();

        profile.authUserId = user.getId();
        profile.role = user.getRole();
        profile.name = user.getName();
        profile.location = request.getLocation();

        // only for LAWYER
        profile.category = request.getCategory();
        profile.experience = request.getExperience();
        profile.fees = request.getFees();

        // 🔥 CALL USERSERVICE
        restClient.post()
                .uri("http://localhost:8082/api/users/create")
                .body(profile)
                .retrieve()
                .toBodilessEntity();
        
        return "User Registered Successfully";
    }

   public AuthResponse login(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid password");
    }

    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

    return new AuthResponse(token);
}
}