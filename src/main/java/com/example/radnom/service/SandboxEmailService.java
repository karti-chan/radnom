// src/main/java/com/example/radnom/service/SandboxEmailService.java
package com.example.radnom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("sandbox")
public class SandboxEmailService implements EmailService {
    
    private final JavaMailSender mailSender;
    
    public SandboxEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Value("${app.test.email:test@example.com}")  // DODAJ
    private String testEmail;
    
    @Value("${app.safe.mode:true}")  // DODAJ
    private boolean safeMode;
    
    @Override
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("hello@demomailtrap.co");
        
        mailSender.send(message);
    }
    
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String subject = "Resetowanie hasła";
        String text = "Kliknij link: " + resetLink;
        sendEmail(toEmail, subject, text);
    }
}