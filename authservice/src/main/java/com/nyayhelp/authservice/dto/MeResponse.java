package com.nyayhelp.authservice.dto;

public class MeResponse {
    public Long id;
    public String name;
    public String email;
    public String role;

    public MeResponse(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
