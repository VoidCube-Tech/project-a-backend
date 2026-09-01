package com.voidcube.tech.projectA.shared.service;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.shared.config.AppMailProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendingService implements EmailService {

    private final JavaMailSender mailSender;
    private final AppMailProperties mailProperties;

    @Override
    public EmailSendResult sendVerificationEmail(
            String recipient,
            String verificationLink
    ) {
        SimpleMailMessage message =
                buildVerificationMessage(recipient, verificationLink);

        return send(message);
    }

    private SimpleMailMessage buildVerificationMessage(
            String recipient,
            String verificationLink
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailProperties.from());
        message.setTo(recipient);
        message.setSubject(mailProperties.verificationSubject());
        message.setText(
                "Clique no link para verificar sua conta: "
                        + verificationLink
        );

        return message;
    }

    private EmailSendResult send(SimpleMailMessage message) {
        try {
            mailSender.send(message);
            return EmailSendResult.SENT;
        } catch (MailException exception) {
            log.error(
                    "Falha ao enviar e-mail de verificação.",
                    exception
            );

            return EmailSendResult.FAILED;
        }
    }
}