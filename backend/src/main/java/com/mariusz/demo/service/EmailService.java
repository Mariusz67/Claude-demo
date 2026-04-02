package com.mariusz.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${mail.from}")
    private String from;

    public boolean sendReminder(String to, String noteText) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("from", from);
            payload.put("to", to);
            payload.put("subject", "Reminder: " + truncate(noteText, 60));
            payload.put("text", "Hello,\n\nHere is your scheduled reminder:\n\n" + noteText + "\n\nBest regards,\nMemoBee");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Reminder sent to {}", to);
                return true;
            } else {
                log.error("Failed to send reminder to {}: HTTP {} - {}", to, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send reminder to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public boolean sendPasswordReset(String to, String resetLink) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("from", from);
            payload.put("to", to);
            payload.put("subject", "Password Reset - MemoBee");
            payload.put("text", "Hello,\n\nYou requested a password reset. Click the link below to set a new password:\n\n"
                    + resetLink + "\n\nThis link expires in 15 minutes.\n\nIf you did not request this, please ignore this email.\n\nBest regards,\nMemoBee");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Password reset email sent to {}", to);
                return true;
            } else {
                log.error("Failed to send password reset to {}: HTTP {} - {}", to, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send password reset to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
