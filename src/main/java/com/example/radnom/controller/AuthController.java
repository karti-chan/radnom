package com.example.radnom.controller;  // ← GŁÓWNY PAKIET!

import com.example.radnom.config.JwtUtils;
import com.example.radnom.entity.User;
import com.example.radnom.repository.UserRepository;
import com.example.radnom.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"},
        maxAge = 3600,
        allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    
    // KONSTRUKTOR zamiast @Autowired
    public AuthController(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         JwtUtils jwtUtils,
                         EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService; 
        System.out.println("🎉🎉🎉 AUTH CONTROLLER UTWORZONY W GŁÓWNYM PAKIECIE!");
    }
    
    @GetMapping("/test")
    public String test() {
        return "✅ Auth Controller działa w głównym pakiecie!";
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("📨 Rejestracja: " + request.getUsername());
        
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            userRepository.save(user);
            System.out.println("✅ Użytkownik zapisany: " + user.getUsername());
            
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
            var user = userRepository.findByUsername(request.getUsername());
            
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            if (!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }
            
            String token = jwtUtils.generateJwtToken(user.get().getUsername());
            
            return ResponseEntity.ok(new JwtResponse(
                token,
                "Bearer",
                user.get().getUsername(),
                user.get().getRole()
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Błąd logowania: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        System.out.println("📧 Forgot password request for: " + request.getEmail());
        
        try {
            // 1. Znajdź użytkownika po emailu
            var userOpt = userRepository.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                
                // Dla bezpieczeństwa zwracamy taką samą odpowiedź
                return ResponseEntity.ok(Map.of(
                    "message", "Jeśli konto istnieje, email z linkiem resetującym został wysłany",
                    "status", "success"
                ));
            }
            
            User user = userOpt.get();
            
            // 2. Generuj token resetujący (przykład - w rzeczywistości użyj JWT lub innego mechanizmu)
            String resetToken = jwtUtils.generatePasswordResetToken(user.getUsername());
            
            // 3. Zapisz token w bazie (dodaj pole do User entity)
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordExpires(System.currentTimeMillis() + 3600000); // 1 godzina
            userRepository.save(user);
            
            // 4. Stwórz link resetujący
            String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

            // 5. Wyślij email (debug)
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            System.out.println("✅ Reset token dla " + user.getEmail() + ": " + resetToken);
            System.out.println("🔗 Link: " + resetLink);

            return ResponseEntity.ok(Map.of(
                "message", "Jeśli konto istnieje, email z linkiem resetującym został wysłany",
                "status", "success",
                "debugLink", resetLink  // dla łatwiejszego testowania
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Błąd forgot-password: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        System.out.println("🔄 Reset password request, token: " + request.getToken());
        
        try {
            // 1. Znajdź użytkownika po tokenie
            var userOpt = userRepository.findByResetPasswordToken(request.getToken());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid or expired token");
            }
            
            User user = userOpt.get();
            
            // 2. Sprawdź czy token nie wygasł
            if (user.getResetPasswordExpires() < System.currentTimeMillis()) {
                return ResponseEntity.badRequest().body("Token has expired");
            }
            
            // 3. Sprawdź czy hasła są takie same
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Passwords do not match");
            }
            
            // 4. Zaktualizuj hasło
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setResetPasswordToken(null); // Wyczyść token
            user.setResetPasswordExpires(null); // Wyczyść expiry
            userRepository.save(user);
            
            System.out.println("✅ Hasło zresetowane dla: " + user.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully",
                "status", "success"
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Błąd reset-password: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

        @PostMapping("/test-post")
    public ResponseEntity<?> testPost(@RequestBody Map<String, String> request) {
        System.out.println("✅ Test POST received: " + request);
        
        // Test czy emailService działa
        if (request.containsKey("email")) {
            String testLink = "http://localhost:5173/reset-password?token=test123";
            emailService.sendPasswordResetEmail(request.get("email"), testLink);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Test POST works!",
            "timestamp", System.currentTimeMillis(),
            "requestData", request
        ));
    }

    @PostMapping("/ping")
public String ping() {
    return "PONG - " + System.currentTimeMillis();
}
    
    // ============ KLASY DTO ============
    
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
    
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
        
        public String getToken() { return token; }
        public String getType() { return type; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        
        public void setToken(String token) { this.token = token; }
        public void setType(String type) { this.type = type; }
        public void setUsername(String username) { this.username = username; }
        public void setRole(String role) { this.role = role; }
    }
    
    public static class ForgotPasswordRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        private String confirmPassword;
        
        public String getToken() { return token; }
        public String getNewPassword() { return newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        
        public void setToken(String token) { this.token = token; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}