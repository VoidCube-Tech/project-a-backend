package com.voidcube.tech.projectA.dashboard.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.voidcube.tech.projectA.dashboard.dto.response.DashboardMetricsResponseDTO;
import com.voidcube.tech.projectA.dashboard.dto.response.ProductMetricResponseDTO;
import com.voidcube.tech.projectA.dashboard.service.DashboardMetricsService;
import com.voidcube.tech.projectA.shared.config.SecurityConfig;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardMetricsController.class)
@Import(SecurityConfig.class)
class DashboardMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardMetricsService dashboardMetricsService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarMetricasNaRotaCorreta()
            throws Exception {
        DashboardMetricsResponseDTO response =
                new DashboardMetricsResponseDTO(
                        List.of(
                                new ProductMetricResponseDTO(
                                        10L,
                                        "Camiseta",
                                        12L
                                )
                        ),
                        List.of(
                                new ProductMetricResponseDTO(
                                        20L,
                                        "Curso",
                                        4L
                                )
                        )
                );

        when(dashboardMetricsService.getMetrics())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/dashboard/metrics")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.mostViewedProducts[0].productId"
                        ).value(10L)
                )
                .andExpect(
                        jsonPath(
                                "$.mostViewedProducts[0].eventCount"
                        ).value(12L)
                )
                .andExpect(
                        jsonPath(
                                "$.mostAddedToCartProducts[0].productId"
                        ).value(20L)
                )
                .andExpect(
                        jsonPath(
                                "$.mostAddedToCartProducts[0].eventCount"
                        ).value(4L)
                );

        verify(dashboardMetricsService).getMetrics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDeveExporDashboardNaRaizDaAplicacao()
            throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(dashboardMetricsService);
    }

    @Test
    void deveNegarAcessoAnonimoAsMetricas()
            throws Exception {
        mockMvc.perform(
                        get("/api/v1/dashboard/metrics")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(dashboardMetricsService);
    }
}
