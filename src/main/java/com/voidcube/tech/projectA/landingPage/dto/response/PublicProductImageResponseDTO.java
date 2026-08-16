package com.voidcube.tech.projectA.landingpage.dto.response;

import com.voidcube.tech.projectA.product.model.ProductImage;

public record PublicProductImageResponseDTO(
    String imageUrl,
    boolean main
) {
    
    public static PublicProductImageResponseDTO from(ProductImage image) {
        return new PublicProductImageResponseDTO(image.getImageUrl(), image.isMain());
    }
}
