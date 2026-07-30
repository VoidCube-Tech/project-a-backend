package com.voidcube.tech.projectA.shared.service;

public interface EmailService {
    EmailSendResult sendVerificationEmail(String recipient, String verificationLink);
}
