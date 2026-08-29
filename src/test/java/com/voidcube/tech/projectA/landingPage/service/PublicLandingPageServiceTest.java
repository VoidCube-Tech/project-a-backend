package com.voidcube.tech.projectA.landingpage.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import com.voidcube.tech.projectA.analyticsevent.service.AnalyticsEventService;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicProductResponseDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicProductVariationResponseDTO;
import com.voidcube.tech.projectA.landingpage.model.LandingPage;
import com.voidcube.tech.projectA.landingpage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.model.ProductVariation;
import com.voidcube.tech.projectA.promotion.service.PromotionPriceService;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.exception.WhatsappNotConfiguredException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicLandingPageServiceTest {

    @Mock
    private LandingPageRepository landingPageRepository;

    @Mock
    private PromotionPriceService promotionPriceService;

    @Mock
    private AnalyticsEventService analyticsEventService;

    @InjectMocks
    private PublicLandingPageService publicLandingPageService;

    @Test
    void deveRetornarContratoPublicoCompleto() {
        LandingPage landingPage = createLandingPageWithProduct();
        Product product = landingPage.getProducts().iterator().next();

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));
        when(promotionPriceService.calculateFinalPrice(product))
                .thenReturn(new BigDecimal("75.00"));

        PublicLandingPageResponseDTO response =
                publicLandingPageService.findByDomainUrl("MINHA-LOJA");

        assertEquals(10L, response.landingPageId());
        assertEquals(1, response.products().size());

        PublicProductResponseDTO productResponse = response.products().getFirst();

        assertEquals(20L, productResponse.productId());
        assertEquals(new BigDecimal("100.00"), productResponse.originalPrice());
        assertEquals(new BigDecimal("75.00"), productResponse.finalPrice());
        assertEquals(ProductType.PHYSICAL, productResponse.productType());
        assertEquals(15, productResponse.stockQuantity());
        assertTrue(productResponse.available());
        assertEquals(1, productResponse.variations().size());

        PublicProductVariationResponseDTO variation =
                productResponse.variations().getFirst();

        assertEquals(30L, variation.variationId());
        assertEquals("Tamanho", variation.variationName());
        assertEquals("M", variation.variationValue());
        assertEquals(5, variation.stockQuantity());
    }

    @Test
    void deveCriarRedirecionamentoGeralParaWhatsapp() {
        LandingPage landingPage = createLandingPageWithProduct();

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));

        URI redirectUri =
                publicLandingPageService.buildWhatsappRedirect("MINHA-LOJA", null);

        assertEquals("https", redirectUri.getScheme());
        assertEquals("wa.me", redirectUri.getHost());
        assertEquals("/91999999999", redirectUri.getPath());
        assertEquals(
                "text=Olá! Gostaria de mais informações sobre a loja Minha Loja.",
                decodeQuery(redirectUri)
        );

        verify(analyticsEventService).saveWhatsappClickAsync(10L, null);
    }

    @Test
    void deveCriarRedirecionamentoParaProdutoAssociado() {
        LandingPage landingPage = createLandingPageWithProduct();

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));

        URI redirectUri =
                publicLandingPageService.buildWhatsappRedirect("minha-loja", 20L);

        assertEquals(
                "text=Olá! Tenho interesse no produto Produto da loja Minha Loja.",
                decodeQuery(redirectUri)
        );

        verify(analyticsEventService).saveWhatsappClickAsync(10L, 20L);
    }

    @Test
    void deveRejeitarProdutoNaoAssociado() {
        LandingPage landingPage = createLandingPageWithProduct();

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));

        assertThrows(
                ProductNotFoundException.class,
                () -> publicLandingPageService.buildWhatsappRedirect("minha-loja", 99L)
        );

        verifyNoInteractions(analyticsEventService);
    }

    @Test
    void deveRejeitarLandingPageSemWhatsapp() {
        LandingPage landingPage = createLandingPageWithProduct();
        landingPage.setWhatsappNumber(null);

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));

        assertThrows(
                WhatsappNotConfiguredException.class,
                () -> publicLandingPageService.buildWhatsappRedirect("minha-loja", null)
        );

        verifyNoInteractions(analyticsEventService);
    }

    @Test
    void deveRedirecionarMesmoComAnalyticsSobrecarregado() {
        LandingPage landingPage = createLandingPageWithProduct();

        when(landingPageRepository.findPublicByDomainUrl("minha-loja"))
                .thenReturn(Optional.of(landingPage));

        doThrow(new TaskRejectedException("Executor cheio"))
                .when(analyticsEventService)
                .saveWhatsappClickAsync(10L, null);

        URI redirectUri =
                publicLandingPageService.buildWhatsappRedirect("minha-loja", null);

        assertNotNull(redirectUri);
        assertEquals("wa.me", redirectUri.getHost());

        verify(analyticsEventService).saveWhatsappClickAsync(10L, null);
    }

    private String decodeQuery(URI uri) {
        return URLDecoder.decode(uri.getRawQuery(), StandardCharsets.UTF_8);
    }

    private LandingPage createLandingPageWithProduct() {
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
        product.setProductType(ProductType.PHYSICAL);
        product.setStockQuantity(15);

        ProductVariation variation = new ProductVariation();
        variation.setId(30L);
        variation.setVariationName("Tamanho");
        variation.setVariationValue("M");
        variation.setStockQuantity(5);

        product.addVariation(variation);
        landingPage.addProduct(product);

        return landingPage;
    }
}