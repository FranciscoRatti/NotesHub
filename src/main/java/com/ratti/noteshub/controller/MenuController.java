package com.ratti.noteshub.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ratti.noteshub.service.NoteService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class MenuController {
  private final NoteService noteService;

  @RequestMapping("")
  public String root() {
    return "redirect:/menu";
  }

  @GetMapping("/menu")
  public String menu(Authentication auth, Model model) {
    if (auth.isAuthenticated()) {
      model.addAttribute("notes", noteService.findByOwner(auth.getName(), false));
      model.addAttribute("trash", noteService.findByOwner(auth.getName(), true));
      return "menu";
    }
    return "redirect:/login";
  }
}
