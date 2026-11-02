package com.ratti.noteshub.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ratti.noteshub.entity.Note;
import com.ratti.noteshub.service.NoteService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfig {
  private static final Logger out = LoggerFactory.getLogger(SchedulingConfig.class);
  private final NoteService noteService;

  @Async
  @Scheduled(cron = "0 0 0 * * *")
  public void checkTrash() {
    int deleteNotes = 0;
    LocalDate thirtyDaysBefore = LocalDate.now().minusDays(30);

    for (Note note : noteService.getOnTrash()) {
      if (note.getTrashDate() != null && note.getTrashDate().isBefore(thirtyDaysBefore)) {
        noteService.remove(note.getId());
        deleteNotes++;
      }
    }

    out.debug("The trash was checked, " + deleteNotes + " notes were deleted");
  }
}
