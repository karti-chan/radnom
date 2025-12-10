package com.example.radnom.service;

import org.springframework.stereotype.Service;

@Service

public interface EmailService {
    void sendEmail(String to, String subject, String text);
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
    
