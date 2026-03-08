package com.mariusz.demo.controller;

import com.mariusz.demo.model.Note;
import com.mariusz.demo.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // GET all notes - admin only (enforced by SecurityConfig)
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        noteRepository.findAll().forEach(notes::add);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // GET note by id - own notes only (or admin)
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin() && !note.get().getUserEmail().equals(getCurrentUserEmail())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(note.get(), HttpStatus.OK);
    }

    // GET notes by user email - own notes only (or admin)
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Note>> getNotesByUserEmail(@PathVariable String userEmail) {
        if (!isAdmin() && !userEmail.equals(getCurrentUserEmail())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Note> notes = noteRepository.findByUserEmail(userEmail);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // GET notes by user email and type - own notes only (or admin)
    @GetMapping("/user/{userEmail}/type/{type}")
    public ResponseEntity<List<Note>> getNotesByUserEmailAndType(
            @PathVariable String userEmail,
            @PathVariable String type) {
        if (!isAdmin() && !userEmail.equals(getCurrentUserEmail())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Note> notes = noteRepository.findByUserEmailAndType(userEmail, type);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // POST create new note - userEmail is taken from token, not request body
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        note.setUserEmail(getCurrentUserEmail());

        if (!isValidType(note.getType())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (note.getFrequency() != null && !isValidFrequency(note.getFrequency())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (note.getText() != null && note.getText().length() > 10000) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Note savedNote = noteRepository.save(note);
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    // PUT update note - own notes only (or admin)
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note noteDetails) {
        Optional<Note> noteData = noteRepository.findById(id);
        if (noteData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin() && !noteData.get().getUserEmail().equals(getCurrentUserEmail())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Note note = noteData.get();

        if (noteDetails.getType() != null) {
            if (!isValidType(noteDetails.getType())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            note.setType(noteDetails.getType());
        }
        if (noteDetails.getText() != null) {
            if (noteDetails.getText().length() > 10000) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            note.setText(noteDetails.getText());
        }
        if (noteDetails.getFrequency() != null) {
            if (!isValidFrequency(noteDetails.getFrequency())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            note.setFrequency(noteDetails.getFrequency());
        }
        if (noteDetails.getAttachmentName() != null) {
            note.setAttachmentName(noteDetails.getAttachmentName());
        }
        if (noteDetails.getAttachmentType() != null) {
            note.setAttachmentType(noteDetails.getAttachmentType());
        }
        if (noteDetails.getAttachmentData() != null) {
            note.setAttachmentData(noteDetails.getAttachmentData());
        }

        return new ResponseEntity<>(noteRepository.save(note), HttpStatus.OK);
    }

    // DELETE note - own notes only (or admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteNote(@PathVariable Long id) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!isAdmin() && !note.get().getUserEmail().equals(getCurrentUserEmail())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        noteRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean isValidType(String type) {
        return type != null && (type.equals("note") || type.equals("memo") || type.equals("reminder"));
    }

    private boolean isValidFrequency(String frequency) {
        return frequency.equals("never") || frequency.equals("daily") ||
               frequency.equals("weekly") || frequency.equals("monthly") ||
               frequency.equals("quarterly") || frequency.equals("yearly");
    }
}
