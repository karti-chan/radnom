package com.example.radnom.config;

import com.example.radnom.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor  // <-- DODAJ to!
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;  // <-- Spring wstrzyknie automatycznie

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // OPTIONS request - pozwól przejść
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("OPTIONS request - skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("\n=== JWT FILTER ===");
        log.info("URI: {}", request.getRequestURI());
        log.info("Method: {}", request.getMethod());

        final var authHeader = request.getHeader("Authorization");

        // Jeśli nie ma Authorization header, przejdź dalej
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        log.info("Token present: {}...", jwt.substring(0, Math.min(20, jwt.length())));

        try {
            // 1. Wyciągnij email z tokena
            String email = jwtService.extractUsername(jwt);
            log.info("Extracted email: {}", email);

            // 2. Sprawdź czy email jest poprawny
            if (email == null || email.isEmpty()) {
                log.warn("Email is null or empty");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: no email");
                return;
            }

            // 3. Załaduj UserDetails z bazy (WAŻNE!)
            log.info("Loading UserDetails for email: {}", email);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            log.info("UserDetails loaded successfully for: {}", userDetails.getUsername());

            // 4. Sprawdź czy token jest ważny dla tego usera
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("Token invalid or expired for user: {}", email);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // 5. Stwórz authentication z UserDetails
            log.info("Creating authentication for: {}", email);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,           // <-- UserDetails, nie String!
                    null,                  // credentials są null dla JWT
                    userDetails.getAuthorities()  // pobierz role z UserDetails
            );

            // 6. Dodaj szczegóły requestu
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. Ustaw w SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("Authentication set in SecurityContext for: {}\n", email);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
            return;
        }

        // 8. Przejdź do następnego filtra
        filterChain.doFilter(request, response);
    }
}