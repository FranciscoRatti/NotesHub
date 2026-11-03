package com.ratti.noteshub.infraestructure.web;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ratti.noteshub.NotesHubApplication;
import com.ratti.noteshub.application.service.AppUserService;
import com.ratti.noteshub.domain.entity.AppUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
        return "login/validation";
      }
    }
    return "user/new";
  }

  // Validar ------------------------------------------------

  @GetMapping("/validate/{token}")
  public String validate(@PathVariable String token, Model model) {
    model.addAttribute("hasError", Boolean.valueOf(userService.validate(token)));
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

  // Cambiar contraseña ------------------------------------------------

  @GetMapping("/recover")
  public String recover(Model model) {
    model.addAttribute("emailDTO", new EmailDTO(""));
    return "user/recover";
  }

  @PostMapping("/recover")
  public String recover(@Valid EmailDTO email, BindingResult errors) {
    if (!errors.hasErrors()) {
      if (userService.existByEmail(email.email)) {
        userService.recover(email.email);
        return "redirect:/login";
      }
      errors.addError(new FieldError("email", "email", "El email no esta asociado a ninguna cuenta"));
    }
    return "user/recover";
  }

  private record EmailDTO(
      @NotNull(message = "Campo requerido") @Email(message = "Email no valido") String email) {
    public EmailDTO(String email) {
      this.email = email;
    }
  }

  @GetMapping("/recover/{token}")
  public String changePassword(@PathVariable String token, Model model) {
    model.addAttribute("hasError",
        Boolean.valueOf(!userService.existByTokenAntType(token, NotesHubApplication.TOKEN_CHANGE_PASSWORD)));
    model.addAttribute("passwordDTO", new PasswordDTO("", ""));
    model.addAttribute("token", token);
    return "user/changePassword";
  }

  @PostMapping("/recover/{token}")
  public String changePassword(@Valid PasswordDTO password, BindingResult errors,
      @PathVariable String token,
      Model model, Authentication auth) {
    model.addAttribute("token", token);

    if (userService.existByTokenAntType(token, NotesHubApplication.TOKEN_CHANGE_PASSWORD)) {
      model.addAttribute("hasError", false);
      if (!errors.hasErrors()) {
        if (password.password.equals(password.repeatedPassword)) {
          userService.changePassword(token, password.password);
          if (auth.isAuthenticated()) {
            return "redirect:/menu";
          }
          return "redirect:/login";
        }
        errors.addError(new FieldError("passwordDTO", "repeatedPassword", "Las contraseñas no coinciden"));
      }
      System.out.println(errors.getAllErrors());
    } else {
      model.addAttribute("hasError", true);
    }
    return "user/changePassword";
  }

  private record PasswordDTO(
      @NotNull(message = "Campo requerido") @Length(min = 8, message = "Contraseña muy corta") String password,
      @NotNull(message = "Campo requerido") @Length(min = 8, message = "Contraseña muy corta") String repeatedPassword) {
    public PasswordDTO(String password, String repeatedPassword) {
      this.password = password;
      this.repeatedPassword = repeatedPassword;
    }
  }
}
