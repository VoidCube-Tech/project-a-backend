package com.voidcube.tech.projectA.landingpage.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicProductResponseDTO;
import com.voidcube.tech.projectA.landingpage.model.LandingPage;
import com.voidcube.tech.projectA.landingpage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.promotion.service.PromotionPriceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicLandingPageServiceTest {

    @Mock
    private LandingPageRepository landingPageRepository;

    @Mock
    private PromotionPriceService promotionPriceService;

    @InjectMocks
    private PublicLandingPageService publicLandingPageService;

    @Test
    void deveRetornarIdsNecessariosParaRegistrarAnalytics() {
        LandingPage landingPage = new LandingPage();
        landingPage.setId(10L);
        landingPage.setName("Minha Loja");
        landingPage.setDomainUrl("minha-loja");
        landingPage.setWhatsappNumber("91999999999");

        Product product = new Product();
        product.setId(20L);
        product.setName("Produto");
        product.setDescription("Descrição");
        product.setPrice(new BigDecimal("100.00"));
        product.setProductType(ProductType.DIGITAL);

        landingPage.addProduct(product);

        when(
                landingPageRepository.findPublicByDomainUrl(
                        "minha-loja"
                )
        ).thenReturn(Optional.of(landingPage));

        when(
                promotionPriceService.calculateFinalPrice(product)
        ).thenReturn(new BigDecimal("75.00"));

        PublicLandingPageResponseDTO response =
                publicLandingPageService.findByDomainUrl(
                        "MINHA-LOJA"
                );

        assertEquals(10L, response.landingPageId());
        assertEquals(1, response.products().size());

        PublicProductResponseDTO productResponse =
                response.products().getFirst();

        assertEquals(20L, productResponse.productId());
        assertEquals(
                new BigDecimal("75.00"),
                productResponse.finalPrice()
        );
    }
}