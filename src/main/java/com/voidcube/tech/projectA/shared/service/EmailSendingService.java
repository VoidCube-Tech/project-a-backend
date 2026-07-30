package com.voidcube.tech.projectA.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.shared.config.AppMailProperties;

@Service
public class EmailSendingService implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailSendingService.class);

    private final JavaMailSender mailSender;
    private final AppMailProperties mailProperties;

    public EmailSendingService(JavaMailSender mailSender, AppMailProperties mailProperties) {

        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

@Override
public EmailSendResult sendVerificationEmail(String recipient, String verificationLink) {
    SimpleMailMessage message = buildVerificationMessage(recipient, verificationLink);
   return send(message);
}

private SimpleMailMessage buildVerificationMessage(String recipient, String verificationLink) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(mailProperties.from());
    message.setTo(recipient);
    message.setSubject(mailProperties.verificationSubject());
    message.setText("Clique no link para verificar sua conta: " + verificationLink);
    return message;
}

private EmailSendResult send(SimpleMailMessage message) {
    try{
        mailSender.send(message);
        return EmailSendResult.SENT;
    } catch (MailException e) {
        logger.error("Falha ao enviar e-mail para {}", message.getTo(), e);
        return EmailSendResult.FAILED;
    }
}

}
