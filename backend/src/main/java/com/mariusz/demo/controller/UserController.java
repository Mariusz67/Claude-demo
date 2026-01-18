package com.mariusz.demo.controller;

import com.mariusz.demo.model.User;
import com.mariusz.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private UserRepository userRepository; //bean injection

    // Admin password pattern: min 15 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern ADMIN_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{15,}$"
    );

    // GET all users 
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add); //using the injected bean
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // GET user by id
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // POST create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // PUT update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword());
            }
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE user
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // POST login user
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> user = userRepository.findByEmailAndPassword(email, password);

        Map<String, Object> response = new HashMap<>();
        if (user.isPresent()) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", user.get());
            response.put("role", user.get().getRole());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // POST create admin user (with strong password validation)
    @PostMapping("/admin")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // Validate admin password strength
        if (user.getPassword() == null || !ADMIN_PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            response.put("success", false);
            response.put("message", "Admin password must be at least 15 characters with 1 uppercase, 1 lowercase, 1 digit, and 1 special character");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        user.setRole("admin");
        User savedUser = userRepository.save(user);

        response.put("success", true);
        response.put("message", "Admin created successfully");
        response.put("user", savedUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Simple health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Backend is running!", HttpStatus.OK);
    }
}
