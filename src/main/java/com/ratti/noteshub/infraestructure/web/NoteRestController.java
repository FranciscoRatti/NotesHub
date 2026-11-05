package com.ratti.noteshub.infraestructure.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratti.noteshub.application.service.NoteService;
import com.ratti.noteshub.domain.entity.Note;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/notes")
public class NoteRestController {
  private final NoteService noteService;

  // Consultar ------------------------------------------------

  @GetMapping("/all")
  public ResponseEntity<List<Note>> all(Authentication auth) {
    if (auth.isAuthenticated()) {
      return ResponseEntity.ok(noteService.findByOwner(auth.getName()));
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Note> find(@PathVariable long id, Authentication auth) {
    if (auth.isAuthenticated()) {
      return ResponseEntity.ok(noteService.findByOwnerAndId(id, auth.getName()));
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // Nueva nota ------------------------------------------------

  @PutMapping("/new")
  public ResponseEntity<Void> insert(@RequestBody NoteRequestEntity note, Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.insert(note.parseNote(), auth.getName());
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // Editar ------------------------------------------------

  @PutMapping("/update/{id}")
  public ResponseEntity<Void> update(@PathVariable long id, @RequestBody NoteRequestEntity note, Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.update(id, note.parseNote());
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  public record NoteRequestEntity(String title, String body, String color) {
    public Note parseNote() {
      return Note.builder()
          .title(title)
          .body(body)
          .color(color)
          .build();
    }
  }

  // Enviar a la papelera ------------------------------------------------

  @DeleteMapping("/trash/{id}")
  public ResponseEntity<Void> trash(@PathVariable long id, Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.trash(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // Restaurar ------------------------------------------------

  @PostMapping("/restore/{id}")
  public ResponseEntity<Void> restore(@PathVariable long id, Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.restore(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // Eliminar ------------------------------------------------

  @DeleteMapping("/remove/{id}")
  public ResponseEntity<Void> remove(@PathVariable long id, Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.remove(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // Limpiar papelera -----------------------------------------------

  @DeleteMapping("/clean-trash")
  public ResponseEntity<Void> clean(Authentication auth) {
    if (auth.isAuthenticated()) {
      noteService.cleanTrash();
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }
}
