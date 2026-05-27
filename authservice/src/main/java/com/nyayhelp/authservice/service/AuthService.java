package com.nyayhelp.authservice.service;

import com.nyayhelp.authservice.dto.AuthResponse;
import com.nyayhelp.authservice.dto.LoginRequest;
import com.nyayhelp.authservice.dto.MeResponse;
import com.nyayhelp.authservice.dto.RegisterRequest;
import com.nyayhelp.authservice.model.User;
import com.nyayhelp.authservice.repository.UserRepository;
import com.nyayhelp.authservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestClient restClient;

    @Value("${nyayhelp.userservice.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${nyayhelp.notificationservice.url:http://localhost:8086}")
    private String notificationServiceUrl;

    public String register(RegisterRequest request) {

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());

        userRepository.save(user);

        createProfile(user, request);

        String event = "LAWYER".equalsIgnoreCase(user.getRole()) ? "LAWYER_SIGNUP" : "CLIENT_SIGNUP";
        sendEmail(user.getEmail(), event, Map.of("name", user.getName() == null ? "" : user.getName()));

        return "User Registered Successfully";
    }

    private static final String ADMIN_USERNAME = "admin123";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final Long ADMIN_USER_ID = 0L;

    public AuthResponse login(LoginRequest request) {

        if (ADMIN_USERNAME.equals(request.getEmail())
                && ADMIN_PASSWORD.equals(request.getPassword())) {
            String token = jwtUtil.generateToken(ADMIN_USER_ID, ADMIN_USERNAME, "ADMIN");
            return new AuthResponse(token);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token);
    }

    public MeResponse me(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        if ("ADMIN".equals(role) && ADMIN_USER_ID.equals(userId)) {
            return new MeResponse(ADMIN_USER_ID, "NyayHelp Admin", ADMIN_USERNAME, "ADMIN");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new MeResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private void createProfile(User user, RegisterRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("authUserId", user.getId());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        body.put("name", user.getName());
        body.put("location", request.getLocation());
        body.put("category", request.getCategory());
        body.put("experience", request.getExperience());
        body.put("fees", request.getFees());

        try {
            restClient.post()
                    .uri(userServiceUrl + "/api/users/internal/create")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Profile creation failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendEmail(String to, String event, Map<String, Object> data) {
        if (to == null || to.isBlank()) return;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("to", to);
            body.put("event", event);
            body.put("data", data);
            restClient.post()
                    .uri(notificationServiceUrl + "/api/notifications/send")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification dispatch failed (event={}, to={}): {}", event, to, e.getMessage());
        }
    }
}
