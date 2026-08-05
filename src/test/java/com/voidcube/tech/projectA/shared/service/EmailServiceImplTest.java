package com.voidcube.tech.projectA.shared.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnabledIfEnvironmentVariable(
        named = "RUN_EMAIL_INTEGRATION_TEST",
        matches = "true"
)
class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    @Test
    void deveEnviarEmailDeVerificacaoComSucesso() {
        EmailSendResult result = emailService.sendVerificationEmail(
                "teste@voidcube.tech",
                "https://voidcube.tech/verificar?token=teste123"
        );

        assertEquals(EmailSendResult.SENT, result);
    }
}
