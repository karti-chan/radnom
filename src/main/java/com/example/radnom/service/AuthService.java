package com.example.radnom.service;

import com.example.radnom.entity.dto.*;
import com.example.radnom.entity.User;
import com.example.radnom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    // ========== REGISTER ==========
    public User registerUser(RegisterRequest request) {
        log.info("Rejestracja użytkownika: username={}, email={}",
                request.getUsername(), request.getEmail());

        validateRegistration(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("Użytkownik zarejestrowany pomyślnie: id={}, username={}",
                savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    private void validateRegistration(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Próba rejestracji z istniejącym username: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Próba rejestracji z istniejącym emailem: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
    }

    // ========== LOGIN ==========
    public Optional<User> authenticateUser(LoginRequest request) {
        log.info("Autentykacja użytkownika: username={}", request.getUsername());

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            log.warn("Nie znaleziono użytkownika: {}", request.getUsername());
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Nieprawidłowe hasło dla użytkownika: {}", request.getUsername());
            return Optional.empty();
        }

        log.info("Użytkownik uwierzytelniony: {}", request.getUsername());
        return Optional.of(user);
    }

    // STARA metoda - dla kompatybilności
    public String generateLoginToken(User user) {
        return jwtService.generateJwtToken(user.getEmail());
    }

    // NOWA metoda - korzysta z UserDetails
    public String generateLoginToken(UserDetails userDetails) {
        return jwtService.generateToken(userDetails);
    }

    // STARA metoda - bez refresh tokena (dla kompatybilności)
    public JwtResponse createJwtResponse(String token, User user) {
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
        return new JwtResponse(token, "Bearer", user.getUsername(), role);
    }

    // NOWA metoda - z refresh tokenem
    public JwtAuthenticationResponse createJwtResponseWithRefresh(String accessToken, String refreshToken, User user) {
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration() / 1000)
                .username(user.getUsername())
                .build();
    }

    // Metoda pomocnicza do konwersji User na UserDetails
    public UserDetails convertToUserDetails(User user) {
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(role)
                .build();
    }

    // ========== FORGOT PASSWORD ==========
    public Map<String, Object> processForgotPassword(ForgotPasswordRequest request) {
        log.info("Przetwarzanie zapomnianego hasła dla email: {}", request.getEmail());

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            log.info("Nie znaleziono użytkownika z emailem: {}", request.getEmail());
            return createSafeForgotPasswordResponse(null);
        }

        User user = userOpt.get();
        String resetToken = jwtService.generatePasswordResetToken(user.getUsername());
        String resetLink = generateResetLink(resetToken);

        saveResetToken(user, resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Token resetowania wygenerowany dla: {}", user.getEmail());
        return createForgotPasswordResponseWithDebug(resetLink);
    }

    private void saveResetToken(User user, String resetToken) {
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(System.currentTimeMillis() + 3600000);
        userRepository.save(user);
    }

    private String generateResetLink(String token) {
        return "http://localhost:5173/reset-password?token=" + token;
    }

    private Map<String, Object> createSafeForgotPasswordResponse(String debugLink) {
        if (debugLink == null) {
            return Map.of(
                    "message", "Jeśli konto istnieje, email z linkiem resetującym został wysłany",
                    "status", "success"
            );
        }
        return Map.of(
                "message", "Jeśli konto istnieje, email z linkiem resetującym został wysłany",
                "status", "success",
                "debugLink", debugLink
        );
    }

    private Map<String, Object> createForgotPasswordResponseWithDebug(String resetLink) {
        return Map.of(
                "message", "Jeśli konto istnieje, email z linkiem resetującym został wysłany",
                "status", "success",
                "debugLink", resetLink
        );
    }

    // ========== RESET PASSWORD ==========
    public Map<String, Object> processPasswordReset(ResetPasswordRequest request) {
        log.info("Przetwarzanie resetowania hasła z tokenem: {}", request.getToken());

        Optional<User> userOpt = userRepository.findByResetPasswordToken(request.getToken());

        if (userOpt.isEmpty()) {
            log.warn("Nieprawidłowy token resetowania: {}", request.getToken());
            throw new IllegalArgumentException("Invalid or expired token");
        }

        User user = userOpt.get();
        validateResetToken(user);
        validatePasswordsMatch(request);

        updateUserPassword(user, request.getNewPassword());

        log.info("Hasło zresetowane pomyślnie dla: {}", user.getUsername());
        return createResetPasswordSuccessResponse();
    }

    private void validateResetToken(User user) {
        if (user.getResetPasswordExpires() < System.currentTimeMillis()) {
            log.warn("Wygasły token resetowania dla użytkownika: {}", user.getUsername());
            throw new IllegalArgumentException("Token has expired");
        }
    }

    private void validatePasswordsMatch(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Hasła nie pasują do siebie dla tokenu: {}", request.getToken());
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private void updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);
    }

    private Map<String, Object> createResetPasswordSuccessResponse() {
        return Map.of(
                "message", "Password has been reset successfully",
                "status", "success"
        );
    }

    // ========== TEST METHODS ==========
    public Map<String, Object> processTestPost(Map<String, String> request) {
        log.info("Przetwarzanie testowego POST: {}", request);

        if (request.containsKey("email")) {
            String testLink = "http://localhost:5173/reset-password?token=test123";
            log.debug("Wysyłanie testowego emaila do: {}", request.get("email"));
            emailService.sendPasswordResetEmail(request.get("email"), testLink);
        }

        return Map.of(
                "message", "Test POST works!",
                "timestamp", System.currentTimeMillis(),
                "requestData", request
        );
    }
}