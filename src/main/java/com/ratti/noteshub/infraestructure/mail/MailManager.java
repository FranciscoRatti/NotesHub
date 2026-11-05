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
  @Value("${app.url}")
  private String baseUrl;

  public MailManager(JavaMailSender sender) {
    this.sender = sender;
  }

  @Value("${spring.mail.username}")
  private String from;

  @Async
  public void sendVerificationMail(String email, String token) {
    sendEmail(email, "Verificar email",
        "<a href=" + baseUrl + "/users/validate/" + token + ">Validar email</a>");
  }

  @Async
  public void sendChangePasswordEmail(String email, String token) {
    sendEmail(email, "Cambiar contraseña",
        "<a href=" + baseUrl + "/users/change-password/" + token + ">Cambiar contraseña</a>");
  }

  @Async
  public void sendChangeEmailEmail(String email, String token) {
    sendEmail(email, "Cambiar email",
        "<a href=" + baseUrl + "/users/change-email/" + token + ">Validar email</a>");
  }

  private void sendEmail(String email, String title, String body) {
    try {
      MimeMessage msg = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
      helper.setFrom(from);
      helper.setTo(email);
      helper.setSubject(title);
      helper.setText(body, true);

      sender.send(msg);
      logger.info("Mail send to " + email);
    } catch (MessagingException | MailException e) {
      System.err.println(e);
    }
  }
}
