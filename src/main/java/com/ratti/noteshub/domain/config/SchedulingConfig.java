package com.ratti.noteshub.domain.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ratti.noteshub.application.service.AppUserService;
import com.ratti.noteshub.application.service.NoteService;
import com.ratti.noteshub.domain.entity.UserTokens;
import com.ratti.noteshub.domain.entity.Note;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfig {
  private static final Logger out = LoggerFactory.getLogger(SchedulingConfig.class);
  @Value("${app.validatio.max-wait}")
  private int maxWait;

  private final NoteService noteService;
  private final AppUserService userService;

  public SchedulingConfig(NoteService noteService, AppUserService userService) {
    this.noteService = noteService;
    this.userService = userService;
  }

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

    out.info("The trash was checked, " + deleteNotes + " notes were deleted");
  }

  @Async
  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  public void checkValidationTokens() {
    if (userService.count() > 0) {
      int deleteUsers = 0;
      LocalDateTime minutesBefore = LocalDateTime.now().minusMinutes(maxWait);

      for (UserTokens validation : userService.allTokens()) {
        if (validation.getDate().isBefore(minutesBefore)) {
          userService.removeToken(validation.getId());
          userService.remove(validation.getUser().getId());
          deleteUsers++;
        }
      }

      out.info("Tokens was checked, " + deleteUsers + " users were deleted");
    }
  }
}
