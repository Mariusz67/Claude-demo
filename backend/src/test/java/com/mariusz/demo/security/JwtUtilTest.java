package com.mariusz.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // 256-bit secret (32 chars) — minimum for HS256
    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256!!";

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, TEST_SECRET);
    }

    @Test
    void generateToken_containsCorrectSubjectAndRole() {
        String token = jwtUtil.generateToken("user@test.com", "user");
        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("user@test.com", claims.getSubject());
        assertEquals("user", claims.get("role", String.class));
    }

    @Test
    void generateToken_adminRole() {
        String token = jwtUtil.generateToken("admin@test.com", "admin");
        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("admin@test.com", claims.getSubject());
        assertEquals("admin", claims.get("role", String.class));
    }

    @Test
    void generateToken_hasExpirationInFuture() {
        String token = jwtUtil.generateToken("user@test.com", "user");
        Claims claims = jwtUtil.extractClaims(token);

        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void isValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken("user@test.com", "user");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken("user@test.com", "user");
        // Flip a character in the signature (last part)
        String tampered = token.substring(0, token.length() - 2) + "XX";
        assertFalse(jwtUtil.isValid(tampered));
    }

    @Test
    void isValid_returnsFalseForGarbageString() {
        assertFalse(jwtUtil.isValid("not.a.valid.jwt"));
    }

    @Test
    void isValid_returnsFalseForExpiredToken() {
        // Build an already-expired token manually
        String expired = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "user")
                .issuedAt(new Date(System.currentTimeMillis() - 100_000))
                .expiration(new Date(System.currentTimeMillis() - 50_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertFalse(jwtUtil.isValid(expired));
    }

    @Test
    void isValid_returnsFalseForTokenSignedWithDifferentKey() {
        String otherSecret = "another-secret-key-that-is-also-long-enough-for-hs256!!";
        String token = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "user")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertFalse(jwtUtil.isValid(token));
    }
}
