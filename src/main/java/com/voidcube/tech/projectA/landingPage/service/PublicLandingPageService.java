package com.voidcube.tech.projectA.landingpage.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.PublicProductResponseDTO;
import com.voidcube.tech.projectA.landingpage.model.LandingPage;
import com.voidcube.tech.projectA.landingpage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.promotion.service.PromotionPriceService;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicLandingPageService {
    
    private final LandingPageRepository landingPageRepository;
    private final PromotionPriceService promotionPriceService;

    @Transactional
    public PublicLandingPageResponseDTO findByDomainUrl( String domainUrl) {

        String normalizedDomainUrl = normalizedDomainUrl(domainUrl);

        LandingPage landingPage = landingPageRepository.findPublicByDomainUrl(normalizedDomainUrl)
            .orElseThrow(() -> new LandingPageNotFoundException(normalizedDomainUrl));

        List<PublicProductResponseDTO> products = landingPage.getProducts()
            .stream()
            .filter(product -> product.isAvailable())
            .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).thenComparing(Product::getId))
            .map(this::toPublicProductResponse)
            .toList();

            return new PublicLandingPageResponseDTO(
                landingPage.getName(),
                landingPage.getWhatsappNumber(),
                products
            );
}

    
    private PublicProductResponseDTO toPublicProductResponse(Product product) {
        BigDecimal finalPrice = promotionPriceService.calculateFinalPrice(product);

        return PublicProductResponseDTO.from(
            product,
            finalPrice
        );
    }


    private String normalizedDomainUrl(String domainUrl) {
        if(domainUrl == null) {
            throw new IllegalArgumentException("O endereço da landing page é obrigatório");
        }

        return domainUrl
            .trim()
            .toLowerCase(Locale.ROOT);
    }
    }
