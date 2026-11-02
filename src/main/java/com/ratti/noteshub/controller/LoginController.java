package com.ratti.noteshub.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.ratti.noteshub.entity.AppUser;
import com.ratti.noteshub.service.AppUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class LoginController {

  private final AppUserService userService;
  private final PasswordEncoder enconder;

  @GetMapping("/login")
  public String login(Authentication auth, Model model) {
    model.addAttribute("auth", auth);
    return "login/login";
  }

  @PostMapping("/logout")
  public String salir(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
    new SecurityContextLogoutHandler().logout(request, response, auth);
    return "redirect:/login?logout";
  }

  @GetMapping("/new-user")
  public String newUser(AppUser user, Model model) {
    model.addAttribute("appUser", user);
    return "login/new-user";
  }

  @PostMapping("/new-user")
  public String newUser(@Valid AppUser user, BindingResult errors) {
    if (!errors.hasErrors()) {
      user.setPassword(enconder.encode(user.getPassword()));
      user.setDate(LocalDate.now());
      userService.insert(user);
      return "redirect:/login";
    }
    return "login/new-user";
  }
}
