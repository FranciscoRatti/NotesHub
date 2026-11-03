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

import com.ratti.noteshub.application.service.AppUserService;
import com.ratti.noteshub.domain.entity.AppUser;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
@RequestMapping("/users")
public class AppUserController {

  private final AppUserService userService;

  // Nuevo usuario ------------------------------------------------

  @GetMapping("/new")
  public String newUser(AppUser user, Model model) {
    model.addAttribute("appUser", user);
    return "user/new";
  }

  @PostMapping("/new")
  public String newUser(@Valid AppUser user, BindingResult errors) {
    if (!(errors.hasErrors() && !(errors.getErrorCount() == 1 && errors.getFieldError().getField().equals("email")))) {
      if (userService.existByUsername(user.getUsername())) {
        errors.addError(new FieldError("appUser", "username", "El usuario ya esta registrado"));
      } else if (userService.existByEmail(user.getEmail())) {
        errors.addError(new FieldError("appUser", "email", "El mail ya esta registrado"));
      } else {
        userService.insert(user);
        return "redirect:/login-validation";
      }
    }
    return "user/new";
  }

  // Validar ------------------------------------------------

  @GetMapping("/validate/{token}")
  public String validate(@PathVariable String token, Model model) {
    model.addAttribute("validate", Boolean.valueOf(userService.validate(token)));
    return "user/validation";
  }

  // Editar ------------------------------------------------

  @GetMapping("/update")
  public String update(Model model, Authentication auth) {
    if (auth.isAuthenticated()) {
      model.addAttribute("appUser", userService.find(auth.getName()));
      return "user/update";
    }
    return "redirect:/login";
  }

  @PostMapping("/update")
  public String newUser(@Valid AppUser user, BindingResult errors, Authentication auth) {
    if (auth.isAuthenticated()) {
      if (!errors.hasErrors()) {
        if (!user.getUsername().equals(auth.getName()) && userService.existByUsername(user.getUsername())) {
          errors.addError(new FieldError("appUser", "username", "El usuario ya esta registrado"));
        } else {
          userService.update(auth.getName(), user);
          return "redirect:/logout";
        }
      }
      return "user/update";
    }
    return "redirect:/login";
  }
}
