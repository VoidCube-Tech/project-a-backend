package com.voidcube.tech.projectA.landingpage.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.landingpage.service.LandingPageService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LandingPageController.class)
@Import(SecurityConfig.class)
class LandingPageControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LandingPageService landingPageService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void deveNegarExclusaoParaUsuarioAnonimo() throws Exception {
        mockMvc.perform(delete("/api/v1/landing-pages/10")
            .with(csrf()))
            .andExpect(status().isForbidden());

        verifyNoInteractions(landingPageService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarExclusaoParaUsuarioSemPapelAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/landing-pages/10")
            .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(landingPageService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBloquearAdminSemCsrf() throws Exception {
        mockMvc.perform(delete("/api/v1/landing-pages/10"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(landingPageService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminExcluirComCsrf() throws Exception {
        mockMvc.perform(delete("/api/v1/landing-pages/10")
            .with(csrf()))
                .andExpect(status().isNoContent());

        verify(landingPageService).delete(10L);
    }
}