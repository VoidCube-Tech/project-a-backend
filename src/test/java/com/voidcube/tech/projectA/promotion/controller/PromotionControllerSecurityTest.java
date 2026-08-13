package com.voidcube.tech.projectA.promotion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.promotion.service.PromotionService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
@Import(SecurityConfig.class)
class PromotionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void deveNegarUsuarioAnonimo() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/100"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioSemPapelAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/100"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirUsuarioAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/100"))
            .andExpect(status().isNoContent());

        verify(promotionService).delete(100L);
    }
}
