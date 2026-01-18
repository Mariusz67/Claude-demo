package com.mariusz.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Table("notes")
public class Note {

    @Id
    private Long id;

    @Column("user_email")
    private String userEmail;

    @Column("created_at")
    private String createdAt;

    private String type; // note, memo, reminder

    private String text; // max 10000 characters

    private String frequency; // never, daily, weekly, monthly, quarterly, yearly

    @Column("attachment_name")
    private String attachmentName;

    @Column("attachment_type")
    private String attachmentType;

    @Column("attachment_data")
    private String attachmentData; // Base64 encoded

    // Constructors
    public Note() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.frequency = "never";
    }

    public Note(String userEmail, String type, String text) {
        this.userEmail = userEmail;
        this.type = type;
        this.text = text;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.frequency = "never";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentData() {
        return attachmentData;
    }

    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
