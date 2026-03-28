package com.mariusz.demo.service;

import com.mariusz.demo.model.Note;
import com.mariusz.demo.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final NoteRepository noteRepository;
    private final EmailService emailService;

    public ReminderScheduler(NoteRepository noteRepository, EmailService emailService) {
        this.noteRepository = noteRepository;
        this.emailService = emailService;
    }

    // Runs every hour
    @Scheduled(fixedRate = 3_600_000)
    public void processReminders() {
        List<Note> reminders = noteRepository.findAllActiveReminders();
        log.info("Checking {} active reminder(s)", reminders.size());

        for (Note note : reminders) {
            if (isDue(note)) {
                emailService.sendReminder(note.getUserEmail(), note.getText());
                note.setLastSentAt(LocalDateTime.now());
                noteRepository.save(note);
            }
        }
    }

    private boolean isDue(Note note) {
        LocalDateTime lastSent = note.getLastSentAt();
        LocalDateTime now = LocalDateTime.now();

        if (lastSent == null) {
            return true;
        }

        return switch (note.getFrequency()) {
            case "daily"     -> lastSent.isBefore(now.minusDays(1));
            case "weekly"    -> lastSent.isBefore(now.minusWeeks(1));
            case "monthly"   -> lastSent.isBefore(now.minusMonths(1));
            case "quarterly" -> lastSent.isBefore(now.minusMonths(3));
            case "yearly"    -> lastSent.isBefore(now.minusYears(1));
            default          -> false;
        };
    }
}
