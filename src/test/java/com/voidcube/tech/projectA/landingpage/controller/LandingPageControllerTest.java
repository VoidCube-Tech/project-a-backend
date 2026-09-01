package com.voidcube.tech.projectA.landingpage.controller;

import com.voidcube.tech.projectA.landingpage.service.LandingPageService;
import com.voidcube.tech.projectA.shared.exception.GlobalExceptionHandler;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LandingPageControllerTest {

    @Mock
    private LandingPageService landingPageService;

    @InjectMocks
    private LandingPageController landingPageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(landingPageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornarCreatedAoAssociarProdutoNovo() {
        when(landingPageService.associateProduct(10L, 20L))
                .thenReturn(true);

        ResponseEntity<Void> response =
                landingPageController.associateProduct(10L, 20L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());

        verify(landingPageService).associateProduct(10L, 20L);
    }

    @Test
    void deveRetornarNoContentAoAssociarProdutoJaAssociado() {
        when(landingPageService.associateProduct(10L, 20L))
                .thenReturn(false);

        ResponseEntity<Void> response =
                landingPageController.associateProduct(10L, 20L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(landingPageService).associateProduct(10L, 20L);
    }

    @Test
    void deveRetornarNoContentAoDesassociarProduto() {
        ResponseEntity<Void> response =
                landingPageController.disassociateProduct(10L, 20L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(landingPageService).disassociateProduct(10L, 20L);
    }

    @Test
    void deveRetornarNotFoundSemRevelarProdutoDeOutroTenant() throws Exception {
        doThrow(new ProductNotFoundException(20L))
                .when(landingPageService)
                .associateProduct(10L, 20L);

        mockMvc.perform(
                post("/api/v1/landing-pages/10/products/20")
        ).andExpect(status().isNotFound());

        verify(landingPageService).associateProduct(10L, 20L);
    }

    @Test
    void deveExporRotaDeleteDeDesassociacao() throws Exception {
        mockMvc.perform(
                delete("/api/v1/landing-pages/10/products/20")
        ).andExpect(status().isNoContent());

        verify(landingPageService).disassociateProduct(10L, 20L);
    }

    @Test
    void deveExcluirLandingPage() throws Exception {
        mockMvc.perform(delete("/api/v1/landing-pages/10"))
                .andExpect(status().isNoContent());

        verify(landingPageService).delete(10L);
    }

    @Test
    void deveRetornarNotFoundQuandoLandingPageNaoexiste() throws Exception {
        doThrow(new LandingPageNotFoundException(10L))
                .when(landingPageService)
                .delete(10L);

        mockMvc.perform(delete("/api/v1/landing-pages/10"))
                .andExpect(status().isNotFound());

        verify(landingPageService).delete(10L);
    }
}
