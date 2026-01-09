package com.example.radnom.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}") // 24h default
    private Long expiration;

    private SecretKey getSigningKey() {
        System.out.println("🔑 JWT Secret loaded (length: " + secretKeyString.length() + ")");

        if (secretKeyString.length() < 32) {
            System.err.println("⚠️ WARNING: JWT secret is too short!");
        }

        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // ✅ JEDNA metoda extractUsername z logami
    public String extractUsername(String token) {
        System.out.println("🔍 extractUsername() called");
        try {
            String username = extractClaim(token, Claims::getSubject);
            System.out.println("✅ Extracted username: " + username);
            return username;
        } catch (Exception e) {
            System.err.println("❌ Error extracting username: " + e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            System.out.println("🔍 Parsing JWT token...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("✅ JWT parsed successfully");
            return claims;
        } catch (Exception e) {
            System.err.println("❌ JWT parsing error: " + e.getMessage());
            System.err.println("❌ Token that failed: " +
                    (token.length() > 50 ? token.substring(0, 50) + "..." : token));
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username)) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ JEDNA poprawiona metoda isTokenValid
    public boolean isTokenValid(String token) {
        try {
            // 1. Spróbuj sparsować token (sprawdza podpis)
            extractAllClaims(token); // jeśli podpis zły, rzuci wyjątek

            // 2. Sprawdź czy nie wygasł
            boolean notExpired = !isTokenExpired(token);

            System.out.println("✅ Token validation: " + (notExpired ? "VALID" : "EXPIRED"));
            return notExpired;

        } catch (Exception e) {
            System.err.println("❌ Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ METODA KTÓREJ POTRZEBUJE AuthController
    public String generateJwtToken(String username) {
        return generateToken(username); // wywołuje istniejącą metodę
    }

    // ✅ METODA KTÓREJ POTRZEBUJE AuthController
    public String generatePasswordResetToken(String username) {
        // Token resetujący z krótszym czasem ważności (1 godzina)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "password_reset");

        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 godzina
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ Generated password reset token for " + username + " (expires in 1h)");
        return token;
    }

    // ✅ JEDNA metoda generateToken z logami
    public String generateToken(String username) {
        System.out.println("🎫 Generating token for: " + username);
        String token = generateToken(new HashMap<>(), username);
        System.out.println("✅ Token generated (first 50 chars): " +
                (token.length() > 50 ? token.substring(0, 50) + "..." : token));
        return token;
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ Generated JWT for " + username + " (expires in " + expiration/1000/60/60 + "h)");
        return token;
    }

    // ✅ DODAJ TĘ METODĘ - pełna walidacja z logami
    public boolean validateToken(String token) {
        System.out.println("🔐 Validating token...");
        try {
            Claims claims = extractAllClaims(token);
            boolean notExpired = !claims.getExpiration().before(new Date());
            System.out.println("✅ Token subject: " + claims.getSubject());
            System.out.println("✅ Token expiration: " + claims.getExpiration());
            System.out.println("✅ Token not expired: " + notExpired);
            return notExpired;
        } catch (Exception e) {
            System.err.println("❌ Token validation error: " + e.getMessage());
            return false;
        }
    }

    // Pomocnicza metoda do debugowania
    public void printTokenInfo(String token) {
        try {
            System.out.println("🔍 JWT Token Info:");
            System.out.println("  Username: " + extractUsername(token));
            System.out.println("  Expiration: " + extractExpiration(token));
            System.out.println("  Is expired: " + isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Cannot parse token: " + e.getMessage());
        }
    }
}