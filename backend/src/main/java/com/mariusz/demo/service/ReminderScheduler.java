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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSent = note.getLastSentAt();

        // New format: reminderAt + optional repeat interval
        if (note.getReminderAt() != null) {
            if (now.isBefore(note.getReminderAt())) {
                return false; // scheduled time not reached yet
            }
            if (lastSent == null) {
                return true; // never sent and time has passed
            }
            if (!Boolean.TRUE.equals(note.getRepeatUntilDeleted())) {
                return false; // one-time reminder, already sent
            }
            int days     = note.getRepeatDays()     != null ? note.getRepeatDays()     : 0;
            int hours    = note.getRepeatHours()    != null ? note.getRepeatHours()    : 0;
            int quarters = note.getRepeatQuarters() != null ? note.getRepeatQuarters() : 0;
            long totalMinutes = (long) days * 24 * 60 + (long) hours * 60 + (long) quarters * 15;
            if (totalMinutes == 0) return false; // repeat enabled but no interval set
            return lastSent.plusMinutes(totalMinutes).isBefore(now);
        }

        // Old format: frequency field
        if (lastSent == null) return true;
        String freq = note.getFrequency() != null ? note.getFrequency() : "never";
        return switch (freq) {
            case "daily"     -> lastSent.isBefore(now.minusDays(1));
            case "weekly"    -> lastSent.isBefore(now.minusWeeks(1));
            case "monthly"   -> lastSent.isBefore(now.minusMonths(1));
            case "quarterly" -> lastSent.isBefore(now.minusMonths(3));
            case "yearly"    -> lastSent.isBefore(now.minusYears(1));
            default          -> false;
        };
    }
}
