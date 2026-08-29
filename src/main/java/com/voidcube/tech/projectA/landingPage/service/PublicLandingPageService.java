package com.voidcube.tech.projectA.landingpage.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.voidcube.tech.projectA.analyticsevent.service.AnalyticsEventService;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicProductResponseDTO;
import com.voidcube.tech.projectA.landingpage.model.LandingPage;
import com.voidcube.tech.projectA.landingpage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.promotion.service.PromotionPriceService;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.exception.WhatsappNotConfiguredException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicLandingPageService {

    private final LandingPageRepository landingPageRepository;

    private final PromotionPriceService promotionPriceService;

    private final AnalyticsEventService analyticsEventService;

    @Transactional(readOnly = true)
    public PublicLandingPageResponseDTO findByDomainUrl(String domainUrl) {
        LandingPage landingPage = findLandingPage(domainUrl);

        List<PublicProductResponseDTO> products = landingPage
            .getProducts()
            .stream()
            .filter(Product::isAvailable)
            .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Product::getId))
                    .map(this::toPublicProductResponse)
                    .toList();

        return new PublicLandingPageResponseDTO(
                landingPage.getId(),
                landingPage.getName(),
                landingPage.getWhatsappNumber(),
                products
        );
    }

    @Transactional(readOnly = true)
    public URI buildWhatsappRedirect(String domainUrl, Long productId) {
        LandingPage landingPage = findLandingPage(domainUrl);

        String whatsappNumber = landingPage.getWhatsappNumber();

        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            throw new WhatsappNotConfiguredException(landingPage.getDomainUrl());
        }

        Product product = findAssociatedProduct(landingPage, productId);

        String message = buildWhatsappMessage(landingPage,product);

        URI redirectUri = UriComponentsBuilder
                .fromUriString("https://wa.me/{whatsappNumber}")
                .queryParam("text", message)
                .buildAndExpand(whatsappNumber)
                .encode()
                .toUri();

        registerWhatsappClick(landingPage.getId(),
            product == null
                ? null
                : product.getId()
        );

        return redirectUri;
    }

    private LandingPage findLandingPage(String domainUrl) {
        String normalizedDomainUrl = normalizeDomainUrl(domainUrl);

        return landingPageRepository.findPublicByDomainUrl(normalizedDomainUrl)
                .orElseThrow(() -> new LandingPageNotFoundException(normalizedDomainUrl));
    }

    private Product findAssociatedProduct(LandingPage landingPage, Long productId) {
        if (productId == null) {
            return null;
        }

        return landingPage
                .getProducts()
                .stream()
                .filter(Product::isAvailable)
                .filter(product -> productId.equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private String buildWhatsappMessage(LandingPage landingPage, Product product) {
        if (product == null) {
            return "Olá! Gostaria de mais informações sobre a loja "+ landingPage.getName() + ".";
        }

        return "Olá! Tenho interesse no produto " + product.getName() + " da loja " + landingPage.getName() + ".";
    }

    private void registerWhatsappClick(Long landingPageId, Long productId) {
        try {
            analyticsEventService.saveWhatsappClickAsync(landingPageId, productId);
        } catch (
                TaskRejectedException exception
        ) {
            log.warn(
                    "Não foi possível registrar o clique no WhatsApp. landingPageId={}, " + "productId={}, motivo={}",
                    landingPageId,
                    productId,
                    exception.getMessage()
            );
        }
    }

    private PublicProductResponseDTO toPublicProductResponse( Product product) {
        BigDecimal finalPrice = promotionPriceService.calculateFinalPrice(product);

        return PublicProductResponseDTO.from(product, finalPrice);
    }

    private String normalizeDomainUrl(String domainUrl) {
        if (domainUrl == null) {
            throw new IllegalArgumentException(
                    "O endereço da landing page é obrigatório ");
        }

        return domainUrl
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}