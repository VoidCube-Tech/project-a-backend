package com.voidcube.tech.projectA.landingpage.dto.response;

import com.voidcube.tech.projectA.product.model.ProductVariation;

public record PublicProductVariationResponseDTO(
    Long variationId,
    String variationName,
    String variationValue,
    Integer stockQuantity
) {
    
    public static PublicProductVariationResponseDTO from(
        ProductVariation variation
    ) {
        return new PublicProductVariationResponseDTO(
            variation.getId(),
            variation.getVariationName(),
            variation.getVariationValue(),
            variation.getStockQuantity()
        );
    }
}
