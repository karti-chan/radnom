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

    @Value("${jwt.secret:mySuperSecretKeyThatIsAtLeast256BitsLong123!mySuperSecretKeyThatIsAtLeast256BitsLong123!}")
    private String secretKeyString;

    @Value("${jwt.expiration:86400000}") // 24h default
    private Long expiration;

    private SecretKey getSigningKey() {
        System.out.println("🔑 JWT Secret loaded (length: " + secretKeyString.length() + ")");

        if (secretKeyString.length() < 32) {
            System.err.println("⚠️ WARNING: JWT secret is too short!");
        }

        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.err.println("❌ JWT parsing error: " + e.getMessage());
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

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generowanie tokenu
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
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