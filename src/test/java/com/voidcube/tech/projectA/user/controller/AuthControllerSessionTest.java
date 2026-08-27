package com.voidcube.tech.projectA.user.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.shared.config.SecurityConfig;
import com.voidcube.tech.projectA.shared.service.MessageService;
import com.voidcube.tech.projectA.user.service.AuthService;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        AuthControllerSessionTest.SessionProbeController.class
})
class AuthControllerSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void devePersistirAutenticacaoEAlterarIdDaSessao()
            throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@voidcube.tech",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        when(
                authenticationManager.authenticate(any())
        ).thenReturn(authentication);

        when(
                messageService.get("auth.login.success")
        ).thenReturn("Login realizado com sucesso");

        MockHttpSession sessionBeforeLogin =
                new MockHttpSession();

        String sessionIdBeforeLogin =
                sessionBeforeLogin.getId();

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                .with(csrf())
                                        .session(sessionBeforeLogin)
                                        .contentType(
                                                "application/json"
                                        )
                                        .content("""
                                                {
                                                  "email": "admin@voidcube.tech",
                                                  "password": "Senha123!"
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        MockHttpSession authenticatedSession =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(authenticatedSession);

        assertNotEquals(
                sessionIdBeforeLogin,
                authenticatedSession.getId()
        );

        assertNotNull(
                authenticatedSession.getAttribute(
                        HttpSessionSecurityContextRepository
                                .SPRING_SECURITY_CONTEXT_KEY
                )
        );

        mockMvc.perform(
                        get("/api/v1/test/authenticated")
                                .session(authenticatedSession)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "admin@voidcube.tech"
                        )
                );
    }

    @RestController
    static class SessionProbeController {

        @GetMapping("/api/v1/test/authenticated")
        String authenticated(Authentication authentication) {
            return authentication.getName();
        }
    }

    @Test
        void deveRetornarMensagemTraduzidaAposVerificarEmail()
        throws Exception {
        when(
            messageService.get(
                    "auth.email-verification.success"
            )
        ).thenReturn(
            "E-mail verificado com sucesso. Você já pode fazer login."
        );

        mockMvc.perform(
                    get("/api/v1/auth/verify-email")
                            .param("token", "token-valido")
            )
            .andExpect(status().isOk())
            .andExpect(
                    content().string(
                            "E-mail verificado com sucesso. "
                                    + "Você já pode fazer login."
                    )
            );

        verify(authService).verifyEmail("token-valido");
        verify(messageService).get(
            "auth.email-verification.success"
    );
}
}
