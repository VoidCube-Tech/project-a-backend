package com.voidcube.tech.projectA.product.dto.response;

import com.voidcube.tech.projectA.product.model.ProductVariation;

public record ProductVariationResponseDTO(

    Long id,
    String variationName,
    String variationValue,
    Integer stockQuantity

) {
    public static ProductVariationResponseDTO from(ProductVariation variation) {
        return new ProductVariationResponseDTO(variation.getId(), variation.getVariationName(), variation.getVariationValue(), variation.getStockQuantity());
    }
}
