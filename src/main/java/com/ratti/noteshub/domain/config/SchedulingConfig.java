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

import com.ratti.noteshub.NotesHubApplication;
import com.ratti.noteshub.application.service.AppUserService;
import com.ratti.noteshub.application.service.NoteService;
import com.ratti.noteshub.domain.entity.Note;
import com.ratti.noteshub.domain.entity.UserTokens;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfig {
  private static final Logger out = LoggerFactory.getLogger(SchedulingConfig.class);
  @Value("${app.validatio.max-wait-validation}")
  private int maxWaitValidation;
  @Value("${app.validatio.max-wait-change-password}")
  private int maxWaitChangePassword;

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
      int deleteTokens = 0;
      int deleteUsers = 0;
      LocalDateTime minutesBeforeValidation = LocalDateTime.now().minusMinutes(maxWaitValidation);
      LocalDateTime minutesBeforeChangePassword = LocalDateTime.now().minusMinutes(maxWaitChangePassword);

      for (UserTokens validation : userService.allTokens()) {
        switch (validation.getType()) {
          case NotesHubApplication.TOKEN_VALIDATE -> {
            if (validation.getDate().isBefore(minutesBeforeValidation)) {
              userService.removeToken(validation.getId());
              deleteTokens++;

              userService.remove(validation.getUser().getId());
              deleteUsers++;
            }
          }

          case NotesHubApplication.TOKEN_CHANGE_PASSWORD -> {
            if (validation.getDate().isBefore(minutesBeforeChangePassword)) {
              userService.removeToken(validation.getId());
              deleteTokens++;
            }
          }
        }
      }

      out.info(
          "Tokens was checked, " + deleteTokens + " tokens were deleted and " + deleteUsers + " users were deleted");
    }
  }
}
