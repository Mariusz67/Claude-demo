package com.mariusz.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("password_reset_tokens")
public class PasswordResetToken {

    @Id
    private Long id;

    @Column("user_email")
    private String userEmail;

    private String token;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    public PasswordResetToken() {}

    public PasswordResetToken(String userEmail, String token, LocalDateTime expiresAt) {
        this.userEmail = userEmail;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
