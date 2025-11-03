package com.pwr_zpi.reservespotapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Hello, " + auth.getName() + "! Your roles are: " + auth.getAuthorities();
    }
}