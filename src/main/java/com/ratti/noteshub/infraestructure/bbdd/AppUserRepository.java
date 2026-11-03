package com.ratti.noteshub.infraestructure.bbdd;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ratti.noteshub.domain.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  Optional<AppUser> findByEmail(String email);

  void deleteByUsername(String username);
}
