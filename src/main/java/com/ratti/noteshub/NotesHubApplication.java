package com.ratti.noteshub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotesHubApplication {
  public static void main(String[] args) {
    SpringApplication.run(NotesHubApplication.class, args);
  }

  public static final byte TOKEN_VALIDATE = 0;
  public static final byte TOKEN_CHANGE_PASSWORD = 1;
  public static final byte TOKEN_CHANGE_MAIL = 2;
}
