package com.voidcube.tech.projectA.sale.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.sale.dto.request.SaleRequestDTO;
import com.voidcube.tech.projectA.sale.dto.response.SaleResponseDTO;
import com.voidcube.tech.projectA.sale.model.SaleStatus;
import com.voidcube.tech.projectA.sale.service.SaleService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@Import(SecurityConfig.class)
class SaleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminRegistrarVendaComCsrf() throws Exception {
        when(saleService.create(any(SaleRequestDTO.class)))
                .thenReturn(response());

        mockMvc.perform(
                        post("/api/v1/sales")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequest())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));

        verify(saleService).create(any(SaleRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBloquearRegistroSemCsrf() throws Exception {
        mockMvc.perform(
                        post("/api/v1/sales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequest())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(saleService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarRegistroParaUsuarioSemPapelAdmin() throws Exception {
        mockMvc.perform(
                        post("/api/v1/sales")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequest())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(saleService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminCancelarVendaComCsrf() throws Exception {
        when(saleService.cancel(100L)).thenReturn(cancelledResponse());

        mockMvc.perform(
                        post("/api/v1/sales/100/cancel")
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(saleService).cancel(100L);
    }

    @Test
    void deveNegarListagemParaUsuarioAnonimo() throws Exception {
        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(saleService);
    }

    private SaleResponseDTO response() {
        return new SaleResponseDTO(
                100L,
                "Maria",
                "91999999999",
                SaleStatus.CONFIRMED,
                new BigDecimal("150.00"),
                5L,
                null,
                null,
                null,
                List.of()
        );
    }

    private SaleResponseDTO cancelledResponse() {
        return new SaleResponseDTO(
                100L,
                "Maria",
                "91999999999",
                SaleStatus.CANCELLED,
                new BigDecimal("150.00"),
                5L,
                null,
                null,
                5L,
                List.of()
        );
    }

    private String validRequest() {
        return """
                {
                  "customerName": "Maria",
                  "customerPhone": "91999999999",
                  "items": [
                    {
                      "productId": 20,
                      "quantity": 2,
                      "unitPrice": 75.00
                    }
                  ]
                }
                """;
    }
}