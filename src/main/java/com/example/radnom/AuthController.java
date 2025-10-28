package com.example.radnom;

import com.example.radnom.entity.User;
import com.example.radnom.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public AuthController() {
        System.out.println("🎉 AUTH CONTROLLER utworzony!");
    }
    
    @GetMapping("/test")
    public String test() {
        return "✅ Auth Controller działa!";
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("📨 Rejestracja: " + request.getUsername());
        
        try {
            // Sprawdź czy użytkownik już istnieje
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            // Stwórz nowego użytkownika
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            userRepository.save(user);
            System.out.println("✅ Użytkownik zapisany do bazy: " + user.getUsername());
            
            return ResponseEntity.ok("User registered successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ Błąd rejestracji: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        
        public RegisterRequest() {}
    }
}