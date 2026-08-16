package com.voidcube.tech.projectA.landingpage.dto.response;

import java.util.List;

public record PublicLandingPageResponseDTO(

    String name,
    String whatsapNumber,
    List<PublicProductResponseDTO> products
) {}
