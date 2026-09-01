package com.voidcube.tech.projectA.landingpage.dto.response;

import com.voidcube.tech.projectA.product.model.ProductImage;

public record PublicProductImageResponseDTO(
    String imageUrl,
    boolean isMain
) {

    public static PublicProductImageResponseDTO from(ProductImage image) {
        String publicImageUrl = "/api/v1/public/product-images/" + image.getId() + "/file";

        return new PublicProductImageResponseDTO(publicImageUrl,image.isMain());
    }
}