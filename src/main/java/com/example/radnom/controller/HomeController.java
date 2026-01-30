package com.example.radnom.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "Spring Boot działa!<br>" +
               "<a href='/api/auth/test'>Test AuthController</a><br>" +
               "Endpoint rejestracji: POST /api/auth/register";
    }
    
    @GetMapping("/test")
    public String test() {
        return "Backend działa poprawnie!";
    }
}//wywalić, pomyśleć, raczej wywalić xD