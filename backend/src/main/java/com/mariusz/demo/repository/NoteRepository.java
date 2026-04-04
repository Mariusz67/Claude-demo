package com.mariusz.demo.repository;

import com.mariusz.demo.model.Note;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends CrudRepository<Note, Long> {

    List<Note> findByUserEmail(String userEmail);

    @Query("SELECT * FROM notes WHERE type = 'reminder' AND (frequency != 'never' OR reminder_at IS NOT NULL)")
    List<Note> findAllActiveReminders();

    long countByUserEmail(String userEmail);
}
