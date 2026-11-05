package com.ratti.noteshub.domain.entity;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notes")
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotNull
  @Builder.Default
  @ColumnDefault("'Nueva nota'")
  @NotNull(message = "La nota debe tener un titulo")
  private String title = "Nueva nota";

  @Column(columnDefinition = "TEXT")
  private String body;

  @Builder.Default
  private LocalDate date = LocalDate.now();

  @NotNull
  @Builder.Default
  @ColumnDefault("'ffff00'")
  private String color = "ffff00";

  public boolean verifyColor() {
    if (color == null)
      return false;
    if (color.length() != 6)
      return false;

    for (byte b : color.getBytes()) {
      switch (b) {
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f':
          break;
        default:
          return false;
      }
    }

    return true;
  }

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "id")
  private AppUser owner;

  @NotNull
  @Builder.Default
  @ColumnDefault("false")
  private boolean onTrash = false;

  private LocalDate trashDate;
}
