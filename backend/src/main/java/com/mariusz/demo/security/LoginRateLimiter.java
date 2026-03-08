package com.mariusz.demo.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 15 * 60 * 1000; // 15 minutes

    // key = IP address, value = [failureCount, firstAttemptTimestamp]
    private final ConcurrentHashMap<String, long[]> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        long[] data = attempts.get(ip);
        if (data == null) return false;
        if (System.currentTimeMillis() - data[1] > LOCKOUT_MS) {
            attempts.remove(ip);
            return false;
        }
        return data[0] >= MAX_ATTEMPTS;
    }

    public void recordFailure(String ip) {
        attempts.compute(ip, (key, data) -> {
            if (data == null || System.currentTimeMillis() - data[1] > LOCKOUT_MS) {
                return new long[]{1, System.currentTimeMillis()};
            }
            data[0]++;
            return data;
        });
    }

    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    public long remainingLockoutSeconds(String ip) {
        long[] data = attempts.get(ip);
        if (data == null) return 0;
        long elapsed = System.currentTimeMillis() - data[1];
        return Math.max(0, (LOCKOUT_MS - elapsed) / 1000);
    }
}
