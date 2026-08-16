package com.voidcube.tech.projectA.landingpage.dto.response;

import com.voidcube.tech.projectA.landingpage.model.LandingPage;

public record LandingPageResponseDTO(
    Long id,
    String name,
    String domainUrl,
    String whatsappNumber

) {

    public static LandingPageResponseDTO from(LandingPage landingPage) {
        return new LandingPageResponseDTO(
            landingPage.getId(),
            landingPage.getName(),
            landingPage.getDomainUrl(),
            landingPage.getWhatsappNumber()
        );
    }
    
}
