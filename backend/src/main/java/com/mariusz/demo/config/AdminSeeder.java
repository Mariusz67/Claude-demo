package com.mariusz.demo.config;

import com.mariusz.demo.model.User;
import com.mariusz.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String email;

    @Value("${ADMIN_PASSWORD:}")
    private String password;

    public AdminSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("AdminSeeder: ADMIN_EMAIL=" + (email == null ? "null" : email.isBlank() ? "(blank)" : email));
        System.out.println("AdminSeeder: ADMIN_PASSWORD=" + (password == null ? "null" : password.isBlank() ? "(blank)" : "(set)"));

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            System.out.println("AdminSeeder: skipping — missing credentials");
            return;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("admin");
        admin.setEncryptionSalt(UUID.randomUUID().toString());
        admin.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(admin);

        System.out.println("Admin account created: " + email);
    }
}
