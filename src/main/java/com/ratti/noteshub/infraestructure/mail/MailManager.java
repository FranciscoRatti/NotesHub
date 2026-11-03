package com.ratti.noteshub.infraestructure.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailManager {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MailManager.class);
  private final JavaMailSender sender;

  public MailManager(JavaMailSender sender) {
    this.sender = sender;
  }

  @Value("${spring.mail.username}")
  private String from;

  @Async
  public void sendEmailVerificationForm(String email, String token) {
    try {
      MimeMessage msg = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
      helper.setFrom(from);
      helper.setTo(email);
      helper.setSubject("Verificacion");
      helper.setText("<a href=http://localhost:8080/validate/" + token + ">Validar email</a>", true);

      sender.send(msg);
      logger.info("Mail send to " + email);
    } catch (MessagingException | MailException e) {
      System.err.println(e);
    }
  }
}
