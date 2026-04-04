package com.mariusz.demo;

import com.mariusz.demo.model.Note;
import com.mariusz.demo.model.User;
import com.mariusz.demo.repository.NoteRepository;
import com.mariusz.demo.repository.PasswordResetTokenRepository;
import com.mariusz.demo.repository.UserRepository;
import com.mariusz.demo.security.JwtUtil;
import com.mariusz.demo.security.LoginRateLimiter;
import com.mariusz.demo.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private PasswordResetTokenRepository resetTokenRepository;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private LoginRateLimiter rateLimiter;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userToken = jwtUtil.generateToken("user@test.com", "user");
        adminToken = jwtUtil.generateToken("admin@test.com", "admin");
        // Reset rate limiter state between tests
        rateLimiter.recordSuccess("127.0.0.1");
    }

    // =========================================================================
    // Public endpoints — no token required
    // =========================================================================

    @Nested
    class PublicEndpoints {

        @Test
        void health_isAccessibleWithoutToken() throws Exception {
            mockMvc.perform(get("/api/users/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void login_isAccessibleWithoutToken() throws Exception {
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"x@test.com\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void register_isAccessibleWithoutToken() throws Exception {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"email\":\"new@test.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void forgotPassword_isAccessibleWithoutToken() throws Exception {
            mockMvc.perform(post("/api/users/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@test.com\"}"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Unauthenticated access — should be rejected
    // =========================================================================

    @Nested
    class UnauthenticatedAccess {

        @Test
        void getUserList_withoutToken_isRejected() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void getNotes_withoutToken_isRejected() throws Exception {
            mockMvc.perform(get("/api/notes"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void createNote_withoutToken_isRejected() throws Exception {
            mockMvc.perform(post("/api/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"note\",\"text\":\"hello\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteUser_withoutToken_isRejected() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Admin-only endpoints — user role should be rejected
    // =========================================================================

    @Nested
    class AdminOnlyEndpoints {

        @Test
        void getUserList_asUser_returns403() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void getUserList_asAdmin_returns200() throws Exception {
            when(userRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        void createUser_asUser_returns403() throws Exception {
            mockMvc.perform(post("/api/users")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"New\",\"email\":\"new@test.com\",\"password\":\"pass1234\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteUser_asUser_returns403() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteUser_asAdmin_returns204() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        void getAllNotes_asUser_returns403() throws Exception {
            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void getAllNotes_asAdmin_returns200() throws Exception {
            when(noteRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Note ownership — users can only access their own notes
    // =========================================================================

    @Nested
    class NoteOwnership {

        @Test
        void getNote_ownedByUser_returns200() throws Exception {
            Note note = new Note("user@test.com", "note", "my note");
            note.setId(1L);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

            mockMvc.perform(get("/api/notes/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }

        @Test
        void getNote_ownedByOtherUser_returns403() throws Exception {
            Note note = new Note("other@test.com", "note", "not yours");
            note.setId(1L);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

            mockMvc.perform(get("/api/notes/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void getNote_ownedByOtherUser_adminCanAccess() throws Exception {
            Note note = new Note("other@test.com", "note", "admin can see");
            note.setId(1L);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

            mockMvc.perform(get("/api/notes/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        void deleteNote_ownedByOtherUser_returns403() throws Exception {
            Note note = new Note("other@test.com", "note", "not yours");
            note.setId(1L);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

            mockMvc.perform(delete("/api/notes/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());

            verify(noteRepository, never()).deleteById(any());
        }

        @Test
        void getUserNotes_otherUser_returns403() throws Exception {
            mockMvc.perform(get("/api/notes/user/other@test.com")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void getUserNotes_ownNotes_returns200() throws Exception {
            when(noteRepository.findByUserEmail("user@test.com")).thenReturn(List.of());

            mockMvc.perform(get("/api/notes/user/user@test.com")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Login and validation logic
    // =========================================================================

    @Nested
    class LoginAndValidation {

        @Test
        void login_correctCredentials_returnsToken() throws Exception {
            User user = new User("Test", "user@test.com", passwordEncoder.encode("password123"));
            user.setRole("user");
            user.setId(1L);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        void login_wrongPassword_returns401() throws Exception {
            User user = new User("Test", "user@test.com", passwordEncoder.encode("password123"));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@test.com\",\"password\":\"wrongpassword\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void register_duplicateEmail_returns409() throws Exception {
            when(userRepository.findByEmail("existing@test.com"))
                    .thenReturn(Optional.of(new User("E", "existing@test.com")));

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"email\":\"existing@test.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isConflict());
        }

        @Test
        void register_shortPassword_returns400() throws Exception {
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"email\":\"new@test.com\",\"password\":\"short\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void register_missingName_returns400() throws Exception {
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"new@test.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createAdmin_weakPassword_returns400() throws Exception {
            mockMvc.perform(post("/api/users/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Admin2\",\"email\":\"a2@test.com\",\"password\":\"weak\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createAdmin_strongPassword_returns201() throws Exception {
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });

            mockMvc.perform(post("/api/users/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Admin2\",\"email\":\"a2@test.com\",\"password\":\"Str0ng!Password#2024\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // =========================================================================
    // Note validation
    // =========================================================================

    @Nested
    class NoteValidation {

        @Test
        void createNote_invalidType_returns400() throws Exception {
            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"invalid\",\"text\":\"hello\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createNote_validType_returns201() throws Exception {
            when(noteRepository.save(any(Note.class))).thenAnswer(inv -> {
                Note n = inv.getArgument(0);
                n.setId(1L);
                return n;
            });

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"note\",\"text\":\"hello\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        void createNote_setsUserEmailFromToken() throws Exception {
            when(noteRepository.save(any(Note.class))).thenAnswer(inv -> {
                Note n = inv.getArgument(0);
                n.setId(1L);
                return n;
            });

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"memo\",\"text\":\"test\",\"userEmail\":\"hacker@evil.com\"}"))
                    .andExpect(status().isCreated());

            // Verify the saved note has the token's email, not the request body's
            verify(noteRepository).save(argThat(note ->
                    "user@test.com".equals(note.getUserEmail())));
        }
    }
}
