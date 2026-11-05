package com.ratti.noteshub.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_tokens")
public class UserTokens {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String token;

  private LocalDateTime date;

  private byte type;

  @ManyToOne
  private AppUser user;

  @Email
  private String newEmail;
}
