package com.voidcube.tech.projectA.promotion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.promotion.dto.request.PromotionRequestDTO;
import com.voidcube.tech.projectA.promotion.service.PromotionService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void deveNegarUsuarioAnonimo()
            throws Exception {
        mockMvc.perform(
                        delete("/api/v1/promotions/100")
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioSemPapelAdmin()
            throws Exception {
        mockMvc.perform(
                        delete("/api/v1/promotions/100")
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirUsuarioAdminExcluir()
            throws Exception {
        mockMvc.perform(
                        delete("/api/v1/promotions/100")
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(promotionService).delete(100L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBloquearAlteracaoSemCsrf()
            throws Exception {
        mockMvc.perform(
                        delete("/api/v1/promotions/100")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBloquearAtualizacaoSemCsrf()
            throws Exception {
        mockMvc.perform(
                        put("/api/v1/promotions/100")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validUpdateBody())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminAtualizarPromocaoComCsrf()
            throws Exception {
        mockMvc.perform(
                        put("/api/v1/promotions/100")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validUpdateBody())
                )
                .andExpect(status().isOk());

        verify(promotionService).update(
                eq(100L),
                any(PromotionRequestDTO.class)
        );
    }

    private String validUpdateBody() {
        return """
            {
              "name": "Promo percentual",
              "active": true,
              "promotionType": "PERCENTAGE",
              "discountPercentage": 15.00
            }
            """;
    }
}