package com.voidcube.tech.projectA.landingpage.dto.response;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductImage;

public record PublicProductResponseDTO(
    String name,
    String description,
    BigDecimal finalPrice,
    List<PublicProductImageResponseDTO> images,
    List<String> tags

) {

    public static PublicProductResponseDTO from(Product product, BigDecimal finalPrice) {
        List<PublicProductImageResponseDTO> images = product.getImages()
            .stream()
            .sorted(Comparator.comparing((ProductImage image) -> image.isMain()).reversed().thenComparing((ProductImage image) -> image.getId()))
            .map(PublicProductImageResponseDTO::from)
            .toList();

            List<String> tags = product.getTags()
                .stream()
                .map(tag -> tag.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

            return new PublicProductResponseDTO(
                product.getName(),
                product.getDescription(),
                finalPrice,
                images,
                tags
            );
            
    }

}