package com.nyayhelp.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lawyer")
public class LawyerController {

    @GetMapping("/test")
    public String test() {
        return "LAWYER ACCESS SUCCESS";
    }
}