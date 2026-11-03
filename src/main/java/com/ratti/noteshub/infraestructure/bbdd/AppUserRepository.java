package com.ratti.noteshub.infraestructure.bbdd;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.ratti.noteshub.domain.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  Optional<AppUser> findByEmail(String email);

  void deleteByUsername(String username);

  @Modifying
  @Query("UPDATE AppUser u SET u.password = ?2 WHERE u.id = ?1")
  void changePassword(long id, String password);
}
