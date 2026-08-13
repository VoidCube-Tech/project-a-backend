package com.voidcube.tech.projectA.promotion.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.voidcube.tech.projectA.promotion.dto.response.PromotionResponseDTO;
import com.voidcube.tech.projectA.promotion.exception.CouponCodeAlreadyExistsException;
import com.voidcube.tech.projectA.promotion.exception.InvalidPromotionException;
import com.voidcube.tech.projectA.promotion.exception.PromotionNotFoundException;
import com.voidcube.tech.projectA.promotion.model.PromotionType;
import com.voidcube.tech.projectA.promotion.service.PromotionService;
import com.voidcube.tech.projectA.shared.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTest {

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private PromotionController promotionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(promotionController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver()
            )
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void deveRetornarCreatedAoCriarPromocao() throws Exception {
        when(promotionService.create(any())).thenReturn(response());

        mockMvc.perform(
            post("/api/v1/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Promo percentual",
                      "active": true,
                      "promotionType": "PERCENTAGE",
                      "discountPercentage": 15.00
                    }
                    """)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.promotionType").value("PERCENTAGE"))
            .andExpect(jsonPath("$.productIds").doesNotExist());

        verify(promotionService).create(any());
    }

    @Test
    void deveListarPromocoesPaginadas() throws Exception {
        when(promotionService.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                List.of(response()),
                PageRequest.of(0, 20),
                1
            ));

        mockMvc.perform(get("/api/v1/promotions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(100))
            .andExpect(
                jsonPath("$.content[0].promotionType")
                    .value("PERCENTAGE")
            );

        verify(promotionService).findAll(any(Pageable.class));
    }

    @Test
    void deveRetornarNoContentAoRemoverPromocao() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/100"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(promotionService).delete(100L);
    }

    @Test
    void deveRejeitarDescontoPercentualZero() throws Exception {
        mockMvc.perform(
            post("/api/v1/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Promo percentual",
                      "active": true,
                      "promotionType": "PERCENTAGE",
                      "discountPercentage": 0.00
                    }
                    """)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string(
                org.hamcrest.Matchers.containsString("discountPercentage")
            ));

        verify(promotionService, never()).create(any());
    }

    @Test
    void deveRetornarConflictParaCupomDuplicado() throws Exception {
        when(promotionService.create(any()))
            .thenThrow(new CouponCodeAlreadyExistsException("SAVE10"));

        mockMvc.perform(
            post("/api/v1/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Cupom",
                      "active": true,
                      "promotionType": "COUPON",
                      "discountValue": 10.00,
                      "couponCode": "SAVE10",
                      "usageLimit": 5
                    }
                    """)
        )
            .andExpect(status().isConflict())
            .andExpect(content().string(
                org.hamcrest.Matchers.containsString("SAVE10")
            ));
    }

    @Test
    void deveRetornarBadRequestParaPromocaoInvalida() throws Exception {
        when(promotionService.create(any()))
            .thenThrow(new InvalidPromotionException(
                "Campos incompatíveis"
            ));

        mockMvc.perform(
            post("/api/v1/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Promo percentual",
                      "active": true,
                      "promotionType": "PERCENTAGE",
                      "discountPercentage": 15.00
                    }
                    """)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Campos incompatíveis"));
    }

    @Test
    void deveRetornarNotFoundSemRevelarPromocaoDeOutroTenant()
        throws Exception {
        doThrow(new PromotionNotFoundException(100L))
            .when(promotionService)
            .delete(100L);

        mockMvc.perform(delete("/api/v1/promotions/100"))
            .andExpect(status().isNotFound());
    }

    private PromotionResponseDTO response() {
        return new PromotionResponseDTO(
            100L,
            "Promo percentual",
            true,
            PromotionType.PERCENTAGE,
            new BigDecimal("15.00"),
            null,
            null,
            null,
            null,
            null
        );
    }
}
