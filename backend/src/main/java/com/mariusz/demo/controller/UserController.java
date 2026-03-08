package com.mariusz.demo.controller;

import com.mariusz.demo.model.User;
import com.mariusz.demo.repository.UserRepository;
import com.mariusz.demo.security.JwtUtil;
import com.mariusz.demo.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter rateLimiter;

    // Admin password pattern: min 15 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern ADMIN_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{15,}$"
    );

    public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, LoginRateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
    }

    // GET all users (admin only - enforced by SecurityConfig)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        users.forEach(u -> u.setPassword(null));
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // GET user by id (admin only)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setPassword(null);
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // POST create new user (admin only)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // PUT update user (admin only)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            User saved = userRepository.save(user);
            saved.setPassword(null);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DELETE user (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // POST login (public)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Map<String, Object> response = new HashMap<>();

        if (rateLimiter.isBlocked(ip)) {
            response.put("success", false);
            response.put("message", "Too many failed login attempts. Try again in " + rateLimiter.remainingLockoutSeconds(ip) + " seconds.");
            return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
        }

        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            rateLimiter.recordSuccess(ip);
            user.setPassword(null);
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", user);
            response.put("role", user.getRole());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        rateLimiter.recordFailure(ip);
        response.put("success", false);
        response.put("message", "Invalid email or password");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // POST create admin user (admin only)
    @PostMapping("/admin")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        if (user.getPassword() == null || !ADMIN_PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            response.put("success", false);
            response.put("message", "Admin password must be at least 15 characters with 1 uppercase, 1 lowercase, 1 digit, and 1 special character");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        user.setRole("admin");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        saved.setPassword(null);

        response.put("success", true);
        response.put("message", "Admin created successfully");
        response.put("user", saved);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // PUT reset password - admin sets new password without knowing old one
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.isEmpty()) {
            response.put("success", false);
            response.put("message", "newPassword is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Password reset successfully for: " + user.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // GET health check (public)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Backend is running!", HttpStatus.OK);
    }
}
