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

  private record UsernameDTO(
      @NotNull(message = "Campo requerido") @Length(min = 3, message = "Nombre de usuario muy corto") String username) {
    public UsernameDTO(String username) {
      this.username = username;
    }
  }

  private record EmailDTO(
      @NotNull(message = "Campo requerido") @Email(message = "Email no valido") String email) {
    public EmailDTO(String email) {
      this.email = email;
    }
  }

  private record PasswordDTO(
      @NotNull(message = "Campo requerido") @Length(min = 8, message = "Contraseña muy corta") String password,
      @NotNull(message = "Campo requerido") @Length(min = 8, message = "Contraseña muy corta") String repeatedPassword) {
    public PasswordDTO(String password, String repeatedPassword) {
      this.password = password;
      this.repeatedPassword = repeatedPassword;
    }
  }

  // Nuevo usuario ------------------------------------------------

  @GetMapping("/new")
  public String newUser(AppUser user, Model model) {
    model.addAttribute("appUser", user);
    return "user/new";
  }

  @PostMapping("/new")
  public String newUser(@Valid AppUser user, BindingResult errors, Model model) {
    if (!(errors.hasErrors() && !(errors.getErrorCount() == 1 && errors.getFieldError().getField().equals("email")))) {
      if (userService.existByUsername(user.getUsername())) {
        errors.addError(new FieldError("appUser", "username", "El usuario ya esta registrado"));
      } else if (userService.existByEmail(user.getEmail(), false)) {
        errors.addError(new FieldError("appUser", "email", "El mail ya esta registrado"));
      } else {
        userService.insert(user);
        model.addAttribute("message", """
            Verifique su casilla de mail, se le envio un correo con un formulario para verificar su email. \n
            Hasta que no se verifique no podra usar la cuenta.
            Dentro de cinco minutos expirara la solicitud.
                  """);
        return "warning/generic-message";
      }
    }
    return "user/new";
  }

  // Validar ------------------------------------------------

  @GetMapping("/validate/{token}")
  public String validate(@PathVariable String token, Model model) {
    if (userService.validate(token)) {
      model.addAttribute("message", "Su mail se valido con exito!");
    } else {
      model.addAttribute("message", "Algo fallo en la validacion, vuelva a crear un usuario");
    }
    return "warning/generic-message";
  }

  // Editar ------------------------------------------------

  @GetMapping("/update")
  public String update(Model model, Authentication auth) {
    if (auth.isAuthenticated()) {
      model.addAttribute("usernameDTO", new UsernameDTO(auth.getName()));
      model.addAttribute("emailDTO", new EmailDTO(userService.findEmailByUsername(auth.getName())));
      return "user/update";
    }
    return "redirect:/login";
  }

  // Cambiar usuario ------------------------------------------------

  @PostMapping("/change-username")
  public String changeUsername(@Valid UsernameDTO user, BindingResult errors, Authentication auth, Model model) {
    if (user.username.equals(auth.getName())) {
      return "redirect:/users/update";
    }

    if (!errors.hasErrors()) {
      if (!user.username.equals(auth.getName()) && userService.existByUsername(user.username)) {
        errors.addError(new FieldError("usernameDTO", "username", "El usuario ya esta registrado"));
      } else {
        userService.update(auth.getName(), user.username);
        return "redirect:/logout";
      }
    }

    model.addAttribute("usernameDTO", new UsernameDTO(auth.getName()));
    model.addAttribute("emailDTO", new EmailDTO(userService.findEmailByUsername(auth.getName())));
    return "user/update";
  }

  // Cambiar email ------------------------------------------------

  @PostMapping("/change-email")
  public String changeEmail(@Valid EmailDTO email, BindingResult errors, Model model, Authentication auth) {
    if (email.email.equals(userService.findEmailByUsername(auth.getName()))) {
      return "redirect:/users/update";
    }

    if (!errors.hasErrors()) {
      if (userService.existByEmail(email.email, true)) {
        errors.addError(new FieldError("emailDTO", "email", "El email ya esta registrado"));
      } else {
        userService.createChangeEmailToken(auth.getName(), email.email);
        model.addAttribute("message",
            "Verifique su casilla de mail, se le envio un correo con un formulario para verificar su email.");
        return "warning/generic-message";
      }
    }

    model.addAttribute("usernameDTO", new UsernameDTO(auth.getName()));
    model.addAttribute("emailDTO", new EmailDTO(userService.findEmailByUsername(auth.getName())));
    return "user/update";
  }

  @GetMapping("/change-email/{token}")
  public String changeEmail(@PathVariable String token, Model model) {
    userService.changeEmail(token);
    return "redirect:/menu";
  }

  // Cambiar contraseña ------------------------------------------------

  @GetMapping("/change-password")
  public String changePassword(Model model, Authentication auth) {
    if (auth != null && auth.isAuthenticated()) {
      userService.createChangePasswordToken(userService.find(auth.getName()).getEmail());
      model.addAttribute("message", "Se ha enviado un email con un formulario para remplazar la contraseña");
      return "warning/generic-message";
    }
    model.addAttribute("emailDTO", new EmailDTO(""));
    return "user/change-password";
  }

  @PostMapping("/change-password")
  public String changePassword(@Valid EmailDTO email, BindingResult errors, Model model) {
    if (!errors.hasErrors()) {
      if (userService.existByEmail(email.email, true)) {
        userService.createChangePasswordToken(email.email);
        model.addAttribute("message", "Se ha enviado un email con un formulario para remplazar la contraseña");
        return "warning/generic-message";
      }
      errors.addError(new FieldError("emailDTO", "email", "El email no esta asociado a ninguna cuenta"));
    }
    return "user/change-password";
  }

  @GetMapping("/change-password/{token}")
  public String changePassword(@PathVariable String token, Model model) {
    if (userService.existByTokenAntType(token, NotesHubApplication.TOKEN_CHANGE_MAIL)) {
      model.addAttribute("passwordDTO", new PasswordDTO("", ""));
      model.addAttribute("token", token);
      return "user/change-password-form";
    }
    model.addAttribute("message", "El token expiro");
    return "warning/generic-message";
  }

  @PostMapping("/change-password/{token}")
  public String changePassword(@Valid PasswordDTO password, BindingResult errors,
      @PathVariable String token,
      Model model, Authentication auth) {
    model.addAttribute("token", token);

    if (userService.existByTokenAntType(token, NotesHubApplication.TOKEN_CHANGE_PASSWORD)) {
      if (!errors.hasErrors()) {
        if (password.password.equals(password.repeatedPassword)) {
          userService.changePassword(token, password.password);
          if (auth != null && auth.isAuthenticated()) {
            return "redirect:/logout";
          }
          return "redirect:/login";
        }
        errors.addError(new FieldError("passwordDTO", "repeatedPassword", "Las contraseñas no coinciden"));
      }
    }
    return "user/change-password-form";
  }
}
