package com.ratti.noteshub.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ratti.noteshub.NotesHubApplication;
import com.ratti.noteshub.domain.entity.AppUser;
import com.ratti.noteshub.domain.entity.UserTokens;
import com.ratti.noteshub.infraestructure.bbdd.AppUserRepository;
import com.ratti.noteshub.infraestructure.bbdd.UserTokensRepository;
import com.ratti.noteshub.infraestructure.mail.MailManager;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AppUserService implements UserDetailsService {
  private static final Logger out = LoggerFactory.getLogger(AppUserService.class);
  @Value("${app.validatio.max-wait-validation}")
  private int maxWaitValidation;
  @Value("${app.validatio.max-wait-change-password}")
  private int maxWaitChangePassword;

  private final MailManager mailManager;
  private final PasswordEncoder encoder;

  private final AppUserRepository userRepo;
  private final UserTokensRepository tokenRepo;

  public AppUserService(MailManager mailManager, PasswordEncoder encoder,
      AppUserRepository userRepo,
      UserTokensRepository tokenRepo) {
    this.mailManager = mailManager;
    this.encoder = encoder;
    this.userRepo = userRepo;
    this.tokenRepo = tokenRepo;
  }

  public long count() {
    return userRepo.count();
  }

  public Iterable<UserTokens> allTokens() {
    return tokenRepo.findAll();
  }

  public AppUser find(String username) {
    return userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
  }

  public boolean existByUsername(String username) {
    return userRepo.findByUsername(username).isPresent();
  }

  public boolean existByEmail(String email, boolean checkIfHaveToken) {
    Optional<AppUser> userOptional = userRepo.findByEmail(email);
    if (userOptional.isPresent()) {
      if (checkIfHaveToken) {
        AppUser user = userOptional.get();
        if (!tokenRepo.findByUserIdAndType(user.getId(), NotesHubApplication.TOKEN_VALIDATE).isPresent()) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean existByTokenAntType(String token, byte type) {
    return tokenRepo.findByTokenAndType(token, type).isPresent();
  }

  public UserDetails loadUserByUsername(String username) {
    Optional<AppUser> optional = userRepo.findByUsername(username);
    AppUser user;
    if (optional.isPresent()) {
      user = optional.get();
    } else {
      user = userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
    if (!tokenRepo.findByUserIdAndType(user.getId(), NotesHubApplication.TOKEN_VALIDATE).isPresent()) {
      return User.withUsername(user.getUsername())
          .password(user.getPassword())
          .build();
    } else {
      throw new UsernameNotFoundException(username);
    }
  }

  public void insert(AppUser user) {
    user.setPassword(encoder.encode(user.getPassword()));
    user.setDate(LocalDate.now());

    String token = UUID.randomUUID().toString();
    mailManager.sendVerificationMail(user.getEmail(), token);
    userRepo.save(user);

    tokenRepo.save(UserTokens.builder()
        .token(token)
        .date(LocalDateTime.now())
        .type(NotesHubApplication.TOKEN_VALIDATE)
        .user(user)
        .build());
  }

  public void update(String username, String newUsername) {
    AppUser user = find(username);
    user.setUsername(newUsername);
    userRepo.save(user);
  }

  public void removeToken(long id) {
    tokenRepo.deleteById(id);
  }

  public void remove(long id) {
    userRepo.deleteById(id);
  }

  public boolean validate(String token) {
    Optional<UserTokens> optional = tokenRepo.findByTokenAndType(token, NotesHubApplication.TOKEN_VALIDATE);
    if (optional.isPresent()) {
      UserTokens validation = optional.get();
      tokenRepo.deleteById(validation.getId());
      if (validation.getDate().isBefore(LocalDateTime.now().minusMinutes(maxWaitValidation))) {
        userRepo.deleteById(validation.getUser().getId());
        out.info("The token for user '" + validation.getUser().getUsername() + "' was expire");
      } else {
        out.info("Token validate for user'" + validation.getUser().getUsername() + "'");
        return true;
      }
    }
    return false;
  }

  public void createChangePasswordToken(String email) {
    AppUser user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

    String token = UUID.randomUUID().toString();
    mailManager.sendChangePasswordEmail(email, token);

    Optional<UserTokens> optional = tokenRepo.findByUserIdAndType(user.getId(),
        NotesHubApplication.TOKEN_CHANGE_PASSWORD);
    if (optional.isPresent()) {
      UserTokens userToken = optional.get();
      userToken.setDate(LocalDateTime.now());
      tokenRepo.save(userToken);
    } else {
      tokenRepo.save(UserTokens.builder()
          .token(token)
          .date(LocalDateTime.now())
          .type(NotesHubApplication.TOKEN_CHANGE_PASSWORD)
          .user(user)
          .build());
    }
  }

  public boolean changePassword(String token, String password) {
    Optional<UserTokens> optional = tokenRepo.findByTokenAndType(token, NotesHubApplication.TOKEN_CHANGE_PASSWORD);
    if (optional.isPresent()) {
      UserTokens validation = optional.get();
      tokenRepo.deleteById(validation.getId());
      if (validation.getDate().isBefore(LocalDateTime.now().minusMinutes(maxWaitChangePassword))) {
        out.info("The token for user '" + validation.getUser().getUsername() + " ' was expire");
      } else {
        userRepo.changePassword(validation.getUser().getId(), encoder.encode(password));
        out.info("Password changed for user '" + validation.getUser().getUsername() + "'");
        return true;
      }
    }
    return false;
  }
}
