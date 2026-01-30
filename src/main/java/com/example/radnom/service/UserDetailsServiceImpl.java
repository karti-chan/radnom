package com.example.radnom.service;

import com.example.radnom.entity.User;
import com.example.radnom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        System.out.println("UserDetailsService loading user: " + identifier);

        // SPRÓBUJ ZNALEŹĆ PO EMAILU (bo token zawiera email)
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> {
                    // Jeśli nie znajdzie po emailu, spróbuj po username
                    System.out.println("Not found by email, trying username: " + identifier);
                    return userRepository.findByUsername(identifier)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
                });

        System.out.println("User found: " + user.getEmail() + " (" + user.getUsername() + ")");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),  // WAŻNE: użyj EMAIL jako username w UserDetails!
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(
                        user.getRole() != null ? user.getRole() : "ROLE_USER"
                ))
        );
    }
}