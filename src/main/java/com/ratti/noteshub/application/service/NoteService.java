package com.ratti.noteshub.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ratti.noteshub.domain.entity.Note;
import com.ratti.noteshub.infraestructure.bbdd.NoteRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
@Transactional
public class NoteService {
  private final NoteRepository noteRepo;
  private final AppUserService userService;

  public List<Note> findByOwner(String user) {
    return noteRepo.findByOwner(userService.find(user).getId());
  }

  public List<Note> findByOwner(String user, boolean onTrash) {
    return noteRepo.findByOwner(userService.find(user).getId(), onTrash);
  }

  public List<Note> getOnTrash() {
    return noteRepo.getOnTrash();
  }

  public Note find(long id) {
    return noteRepo.findById(id).orElseThrow(() -> new NullPointerException("Note with id " + id + " not exist"));
  }

  public Note findByOwnerAndId(long id, String user) {
    return noteRepo.findByOwnerAndId(userService.find(user).getId(), id)
        .orElseThrow(() -> new NullPointerException("Note with id " + id + " not exist"));
  }

  public void insert(Note note, String owner) {
    note.setDate(LocalDate.now());
    note.setOwner(userService.find(owner));
    noteRepo.save(note);
  }

  public void update(long id, Note note) {
    note.setId(id);
    note.setOwner(find(id).getOwner());
    noteRepo.save(note);
  }

  public void trash(long id) {
    Note note = find(id);
    if (!note.isOnTrash()) {
      note.setTrashDate(LocalDate.now());
    }
    note.setOnTrash(true);
    noteRepo.save(note);
  }

  public void restore(long id) {
    Note note = find(id);
    note.setOnTrash(false);
    note.setTrashDate(null);
    noteRepo.save(note);
  }

  public void remove(long id) {
    noteRepo.deleteById(id);
  }

  public void cleanTrash() {
    for (Note note : getOnTrash()) {
      remove(note.getId());
    }
  }
}
