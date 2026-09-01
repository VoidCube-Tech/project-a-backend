package com.voidcube.tech.projectA.landingpage.controller;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.landingpage.service.PublicLandingPageService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;
import com.voidcube.tech.projectA.shared.exception.WhatsappNotConfiguredException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicLandingPageController.class)
@Import(SecurityConfig.class)
class PublicLandingPageControllerTest {

    private static final String ROUTE =
            "/api/v1/public/landing-pages/minha-loja/whatsapp";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicLandingPageService publicLandingPageService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void deveRedirecionarVisitanteParaWhatsappDaLoja() throws Exception {
        URI redirect = URI.create(
                "https://wa.me/91999999999?text=Ola"
        );

        when(publicLandingPageService.buildWhatsappRedirect("minha-loja", null))
                .thenReturn(redirect);

        mockMvc.perform(get(ROUTE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirect.toString()));

        verify(publicLandingPageService)
                .buildWhatsappRedirect("minha-loja", null);
    }

    @Test
    void deveRedirecionarVisitanteComProduto() throws Exception {
        URI redirect = URI.create(
                "https://wa.me/91999999999?text=Produto"
        );

        when(publicLandingPageService.buildWhatsappRedirect("minha-loja", 20L))
                .thenReturn(redirect);

        mockMvc.perform(get(ROUTE).queryParam("productId", "20"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirect.toString()));

        verify(publicLandingPageService)
                .buildWhatsappRedirect("minha-loja", 20L);
    }

    @Test
    void deveRejeitarProductIdNegativo() throws Exception {
        mockMvc.perform(get(ROUTE).queryParam("productId", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publicLandingPageService);
    }

    @Test
    void deveRetornarErroQuandoWhatsappNaoEstaConfigurado()
            throws Exception {
        doThrow(new WhatsappNotConfiguredException("minha-loja"))
                .when(publicLandingPageService)
                .buildWhatsappRedirect("minha-loja", null);

        mockMvc.perform(get(ROUTE))
                .andExpect(status().isUnprocessableContent());

        verify(publicLandingPageService)
                .buildWhatsappRedirect("minha-loja", null);
    }
}
