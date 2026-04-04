package com.mariusz.demo.controller;

import com.mariusz.demo.model.PasswordResetToken;
import com.mariusz.demo.model.User;
import com.mariusz.demo.repository.PasswordResetTokenRepository;
import com.mariusz.demo.repository.NoteRepository;
import com.mariusz.demo.repository.UserRepository;
import com.mariusz.demo.security.JwtUtil;
import com.mariusz.demo.security.LoginRateLimiter;
import com.mariusz.demo.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final NoteRepository noteRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter rateLimiter;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:8000}")
    private String frontendUrl;

    // Admin password pattern: min 15 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern ADMIN_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{15,}$"
    );

    public UserController(UserRepository userRepository, PasswordResetTokenRepository resetTokenRepository,
                           NoteRepository noteRepository, BCryptPasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil, LoginRateLimiter rateLimiter, EmailService emailService) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
        this.emailService = emailService;
    }

    // GET all users (admin only - enforced by SecurityConfig)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        users.forEach(u -> {
            u.setPassword(null);
            u.setNoteCount(noteRepository.countByUserEmail(u.getEmail()));
        });
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
        user.setEncryptionSalt(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
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
            user.setLastLoginAt(LocalDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
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

    // POST change password (authenticated user, requires old password)
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            response.put("success", false);
            response.put("message", "Email, old password and new password are required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (newPassword.length() < 8) {
            response.put("success", false);
            response.put("message", "New password must be at least 8 characters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !passwordEncoder.matches(oldPassword, userOpt.get().getPassword())) {
            response.put("success", false);
            response.put("message", "Current password is incorrect");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        response.put("success", true);
        response.put("message", "Password changed successfully");
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // POST register (public self-registration)
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || name.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Name is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (email == null || !email.contains("@")) {
            response.put("success", false);
            response.put("message", "A valid email is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.length() < 8) {
            response.put("success", false);
            response.put("message", "Password must be at least 8 characters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            response.put("success", false);
            response.put("message", "An account with this email already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");
        user.setEncryptionSalt(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        User saved = userRepository.save(user);
        saved.setPassword(null);

        response.put("success", true);
        response.put("message", "Account created successfully");
        response.put("user", saved);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // POST forgot password (public) - sends reset email
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");

        // Always return success to prevent email enumeration
        response.put("success", true);
        response.put("message", "If an account with that email exists, a reset link has been sent.");

        if (email == null || email.isBlank()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Delete any existing tokens for this user
        resetTokenRepository.deleteByUserEmail(email.trim().toLowerCase());

        // Generate token, expires in 15 minutes (UTC)
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                email.trim().toLowerCase(), token, LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15));
        resetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = frontendUrl + "/reset.html?token=" + token;
        emailService.sendPasswordReset(email.trim().toLowerCase(), resetLink);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // POST reset password with token (public)
    @PostMapping("/reset-password-token")
    public ResponseEntity<Map<String, Object>> resetPasswordWithToken(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            response.put("success", false);
            response.put("message", "Token and new password are required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (newPassword.length() < 8) {
            response.put("success", false);
            response.put("message", "Password must be at least 8 characters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Optional<PasswordResetToken> tokenOpt = resetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            // Clean up expired token if found
            tokenOpt.ifPresent(t -> resetTokenRepository.delete(t));
            response.put("success", false);
            response.put("message", "Invalid or expired reset link. Please request a new one.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        PasswordResetToken resetToken = tokenOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(resetToken.getUserEmail());
        if (userOpt.isEmpty()) {
            resetTokenRepository.delete(resetToken);
            response.put("success", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete used token
        resetTokenRepository.delete(resetToken);

        response.put("success", true);
        response.put("message", "Password has been reset successfully. You can now log in.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // GET health check (public)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Backend is running!", HttpStatus.OK);
    }
}
