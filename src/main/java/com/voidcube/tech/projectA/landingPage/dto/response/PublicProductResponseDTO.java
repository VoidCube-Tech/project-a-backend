package com.voidcube.tech.projectA.landingpage.dto.response;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductImage;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.model.ProductVariation;

public record PublicProductResponseDTO(
    long productId,
    String name,
    String description,
    BigDecimal originalPrice,
    BigDecimal finalPrice,
    ProductType productType,
    Integer stockQuantity,
    boolean available,
    List<PublicProductImageResponseDTO> images,
    List<String> tags,
    List<PublicProductVariationResponseDTO> variations

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

            List<PublicProductVariationResponseDTO> variations =
                product.getVariations()
                    .stream()
                    .sorted(Comparator.comparing(ProductVariation::getVariationName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ProductVariation::getVariationValue, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ProductVariation::getId))
                    .map(PublicProductVariationResponseDTO::from)
                    .toList();

            return new PublicProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                finalPrice,
                product.getProductType(),
                product.getStockQuantity(),
                product.isAvailable(),
                images,
                tags,
                variations
            );
            
    }

}