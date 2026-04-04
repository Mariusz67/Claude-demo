package com.mariusz.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void newIp_isNotBlocked() {
        assertFalse(rateLimiter.isBlocked("192.168.1.1"));
    }

    @Test
    void fourFailures_isNotBlocked() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure("192.168.1.1");
        }
        assertFalse(rateLimiter.isBlocked("192.168.1.1"));
    }

    @Test
    void fiveFailures_isBlocked() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("192.168.1.1");
        }
        assertTrue(rateLimiter.isBlocked("192.168.1.1"));
    }

    @Test
    void successResetsCounter() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure("192.168.1.1");
        }
        rateLimiter.recordSuccess("192.168.1.1");

        // After success, 5 more failures needed to block
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure("192.168.1.1");
        }
        assertFalse(rateLimiter.isBlocked("192.168.1.1"));
    }

    @Test
    void differentIps_areIndependent() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("10.0.0.1");
        }
        assertTrue(rateLimiter.isBlocked("10.0.0.1"));
        assertFalse(rateLimiter.isBlocked("10.0.0.2"));
    }

    @Test
    void remainingLockoutSeconds_returnsZeroForUnknownIp() {
        assertEquals(0, rateLimiter.remainingLockoutSeconds("1.2.3.4"));
    }

    @Test
    void remainingLockoutSeconds_returnsPositiveWhenBlocked() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("192.168.1.1");
        }
        assertTrue(rateLimiter.remainingLockoutSeconds("192.168.1.1") > 0);
    }
}
