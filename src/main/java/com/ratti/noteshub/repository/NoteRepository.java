package com.ratti.noteshub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.ratti.noteshub.entity.Note;

public interface NoteRepository extends CrudRepository<Note, Long> {

  @Query("SELECT n FROM Note n WHERE n.owner.username = ?1")
  List<Note> findByOwner(String username);

  @Query("SELECT n FROM Note n WHERE n.owner.username = ?1 AND n.onTrash = ?2")
  List<Note> findByOwner(String username, boolean onTrash);

  @Query("SELECT n FROM Note n WHERE n.onTrash = true")
  List<Note> getOnTrash();

  @Query("SELECT n FROM Note n WHERE n.id = ?1 AND n.owner.username = ?2")
  Optional<Note> findByOwnerAndId(long id, String user);
}
