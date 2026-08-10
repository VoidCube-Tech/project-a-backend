package com.voidcube.tech.projectA.product.dto.response;

import com.voidcube.tech.projectA.product.model.ProductImage;

public record ProductImageResponseDTO(
    Long id,
    String imageUrl,
    boolean isMain
) {
    
    public static ProductImageResponseDTO from(ProductImage image) {
        return new ProductImageResponseDTO(
            image.getId(),
            image.getImageUrl(),
            image.isMain()
        );
    }
}
