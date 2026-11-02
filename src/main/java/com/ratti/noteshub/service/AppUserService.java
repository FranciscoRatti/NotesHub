package com.ratti.noteshub.service;

import org.slf4j.Logger;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ratti.noteshub.entity.AppUser;
import com.ratti.noteshub.repository.AppUserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AppUserService implements UserDetailsService {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AppUserService.class);
  private final AppUserRepository repo;

  public AppUser find(String username) {
    return repo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
  }

  public UserDetails loadUserByUsername(String username) {
    AppUser u = repo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    return User.withUsername(u.getUsername())
        .password(u.getPassword())
        .build();
  }

  public void insert(AppUser user) {
    repo.save(user);
    logger.debug("New user created", user);
  }
}
