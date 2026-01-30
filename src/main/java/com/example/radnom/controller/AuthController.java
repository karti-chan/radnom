package com.example.radnom.controller;

import com.example.radnom.entity.dto.*;
import com.example.radnom.service.AuthService;
import com.example.radnom.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        log.info("Register request for: {}", request.getUsername());
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsername());

        var userOpt = authService.authenticateUser(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        var user = userOpt.get();
        var userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // Generuj access token i refresh token
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration() / 1000) // sekundy
                .username(user.getUsername())
                .build();

        log.info("User {} logged in successfully", user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Refresh token request");

        String refreshToken = refreshTokenRequest.getRefreshToken();

        try {
            // Sprawdź czy to refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                log.error("Invalid token type for refresh");
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            String username = jwtService.extractUsername(refreshToken);
            var userDetails = userDetailsService.loadUserByUsername(username);

            // Waliduj refresh token
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                log.error("Invalid refresh token for user: {}", username);
                return ResponseEntity.status(401).body("Invalid refresh token");
            }

            // Generuj nowe tokeny
            String newAccessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpiration() / 1000)
                    .username(username)
                    .build();

            log.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }

    // Opcjonalnie dodaj endpoint do wylogowania
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // W przypadku JWT, wylogowanie odbywa się po stronie klienta
        // Możesz dodać blacklistę tokenów jeśli potrzebujesz
        return ResponseEntity.ok("Logout successful");
    }

    /*
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.processForgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Reset password attempt");
        authService.processPasswordReset(request);
        return ResponseEntity.ok("Password reset successfully");
    }
    */

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth service is healthy");
    }
}