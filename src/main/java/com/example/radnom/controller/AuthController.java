package com.example.radnom.controller;

import com.example.radnom.config.JwtUtils;
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
    
    @Autowired
    private JwtUtils jwtUtils;
    
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
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
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
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 Logowanie: " + request.getUsername());
        
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }
            
            String token = jwtUtils.generateJwtToken(user.getUsername());
            
            return ResponseEntity.ok(new JwtResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole()
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Błąd logowania: " + e.getMessage());
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
    
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        public LoginRequest() {}
    }
    
    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private String username;
        private String role;
        
        public JwtResponse(String token, String type, String username, String role) {
            this.token = token;
            this.type = type;
            this.username = username;
            this.role = role;
        }
    }
}