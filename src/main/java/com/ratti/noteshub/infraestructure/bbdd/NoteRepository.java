package com.ratti.noteshub.infraestructure.bbdd;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.ratti.noteshub.domain.entity.Note;

public interface NoteRepository extends CrudRepository<Note, Long> {

  @Query("SELECT n FROM Note n WHERE n.owner.id = ?1 ORDER BY n.title ASC")
  List<Note> findByOwner(long id);

  @Query("SELECT n FROM Note n WHERE n.owner.id = ?1 AND n.onTrash = ?2 ORDER BY n.title ASC")
  List<Note> findByOwner(long id, boolean onTrash);

  @Query("SELECT n FROM Note n WHERE n.onTrash = true ORDER BY n.title ASC")
  List<Note> getOnTrash();

  @Query("SELECT n FROM Note n WHERE n.owner.id = ?1 AND n.id = ?2")
  Optional<Note> findByOwnerAndId(long ownerId, long id);
}
