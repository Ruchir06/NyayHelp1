package com.nyayhelp.authservice.controller;

import com.nyayhelp.authservice.dto.AuthResponse;
import com.nyayhelp.authservice.dto.LoginRequest;
import com.nyayhelp.authservice.dto.RegisterRequest;
import com.nyayhelp.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}