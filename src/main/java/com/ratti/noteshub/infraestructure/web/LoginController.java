package com.ratti.noteshub.infraestructure.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class LoginController {

  @GetMapping("/login")
  public String login(Authentication auth, Model model) {
    model.addAttribute("auth", auth);
    return "login/login";
  }

  @GetMapping("/logout")
  public String salir(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
    new SecurityContextLogoutHandler().logout(request, response, auth);
    return "redirect:/login?logout";
  }
}
