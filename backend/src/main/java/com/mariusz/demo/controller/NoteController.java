package com.mariusz.demo.controller;

import com.mariusz.demo.model.Note;
import com.mariusz.demo.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    // GET all notes
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        noteRepository.findAll().forEach(notes::add);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // GET note by id
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Optional<Note> note = noteRepository.findById(id);
        return note.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // GET notes by user email
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Note>> getNotesByUserEmail(@PathVariable String userEmail) {
        List<Note> notes = noteRepository.findByUserEmail(userEmail);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // GET notes by user email and type
    @GetMapping("/user/{userEmail}/type/{type}")
    public ResponseEntity<List<Note>> getNotesByUserEmailAndType(
            @PathVariable String userEmail,
            @PathVariable String type) {
        List<Note> notes = noteRepository.findByUserEmailAndType(userEmail, type);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    // POST create new note
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        // Validate type
        if (!isValidType(note.getType())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate frequency
        if (note.getFrequency() != null && !isValidFrequency(note.getFrequency())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate text length
        if (note.getText() != null && note.getText().length() > 10000) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Note savedNote = noteRepository.save(note);
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    // PUT update note
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note noteDetails) {
        Optional<Note> noteData = noteRepository.findById(id);

        if (noteData.isPresent()) {
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
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE note
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteNote(@PathVariable Long id) {
        noteRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Helper methods for validation
    private boolean isValidType(String type) {
        return type != null && (type.equals("note") || type.equals("memo") || type.equals("reminder"));
    }

    private boolean isValidFrequency(String frequency) {
        return frequency.equals("never") || frequency.equals("daily") ||
               frequency.equals("weekly") || frequency.equals("monthly") ||
               frequency.equals("quarterly") || frequency.equals("yearly");
    }
}
