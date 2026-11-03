package com.ratti.noteshub.domain.entity;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotNull
  @Length(min = 3, max = 20, message = "Campo requerido")
  @Column(length = 20, unique = true)
  private String username;

  @NotNull
  @Email(message = "Mail no valido")
  @Column(length = 100, unique = true)
  private String email;

  @NotNull
  @Length(min = 8, max = 100, message = "Campo requerido")
  @Column(length = 100)
  private String password;

  private LocalDate date;

  @OneToMany(mappedBy = "owner")
  private List<Note> notes;
}
