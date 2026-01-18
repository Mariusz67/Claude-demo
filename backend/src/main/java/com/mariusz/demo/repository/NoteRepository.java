package com.mariusz.demo.repository;

import com.mariusz.demo.model.Note;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends CrudRepository<Note, Long> {

    List<Note> findByUserEmail(String userEmail);

    List<Note> findByUserEmailAndType(String userEmail, String type);

    List<Note> findByType(String type);
}
