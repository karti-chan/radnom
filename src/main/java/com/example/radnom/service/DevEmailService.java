// src/main/java/com/example/radnom/service/DevEmailService.java
package com.example.radnom.service;

import org.springframework.stereotype.Service;

@Service  // 👈 NIE MA @Profile - to domyślna implementacja!
public class DevEmailService implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String text) {
        System.out.println("=".repeat(50));
        System.out.println("📧 [DEV] EMAIL (nie wysłany)");
        System.out.println("Do: " + to);
        System.out.println("Temat: " + subject);
        System.out.println("Treść: " + text);
        System.out.println("=".repeat(50));
    }
    
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        System.out.println("=".repeat(50));
        System.out.println("📧 [DEV] RESET HASŁA");
        System.out.println("Do: " + toEmail);
        System.out.println("Link: " + resetLink);
        System.out.println("=".repeat(50));
    }
}