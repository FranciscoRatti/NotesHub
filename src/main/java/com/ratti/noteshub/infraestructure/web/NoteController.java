package com.ratti.noteshub.infraestructure.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ratti.noteshub.application.service.NoteService;
import com.ratti.noteshub.domain.entity.Note;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
@RequestMapping("/notes")
public class NoteController {
  private final NoteService noteService;

  // Nueva nota ------------------------------------------------

  @GetMapping("/new")
  public String insert(Model model) {
    model.addAttribute("note", new Note());
    return "note/new";
  }

  @PostMapping("/new")
  public String insert(Authentication auth, @Valid Note note, BindingResult errors) {
    if (!errors.hasErrors()) {
      if (note.verifyColor()) {
        noteService.insert(note, auth.getName());
        return "redirect:/menu";
      }
      errors.addError(new FieldError("note", "color", "El color no es un valor valido"));
    }
    return "note/new";
  }

  // Editar ------------------------------------------------

  @GetMapping("/update/{id}")
  public String update(@PathVariable long id, Model model) {
    model.addAttribute("note", noteService.find(id));
    return "note/update";
  }

  @PostMapping("/update/{id}")
  public String udpate(@PathVariable long id, @Valid Note note, BindingResult errors) {
    if (!errors.hasErrors()) {
      if (note.verifyColor()) {
        noteService.update(id, note);
        return "redirect:/menu";
      }
      errors.addError(new FieldError("note", "color", "El color no es un valor valido"));
    }
    return "note/update";
  }

  // Enviar a la papelera ------------------------------------------------

  @GetMapping("/trash/{id}")
  public String trash(@PathVariable long id) {
    noteService.trash(id);
    return "redirect:/menu";
  }

  // Restaurar ------------------------------------------------

  @GetMapping("/restore/{id}")
  public String restore(@PathVariable long id) {
    noteService.restore(id);
    return "redirect:/menu";
  }

  // Eliminar ------------------------------------------------

  @GetMapping("/remove/{id}")
  public String remove(@PathVariable long id, Model model) {
    model.addAttribute("note", noteService.find(id));
    return "note/remove";
  }

  @PostMapping("/remove/{id}")
  public String remove(@PathVariable long id) {
    noteService.remove(id);
    return "redirect:/trash";
  }

  // Limpiar papelera -----------------------------------------------

  @GetMapping("/clean-trash")
  public String clean() {
    return "note/clean-trash";
  }

  @PostMapping("/clean-trash")
  public String cleanDone() {
    noteService.cleanTrash();
    return "redirect:/trash";
  }
}
