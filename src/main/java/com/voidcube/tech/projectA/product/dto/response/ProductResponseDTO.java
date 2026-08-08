package com.voidcube.tech.projectA.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductType;

public record ProductResponseDTO (

    Long id,
    String name,
    BigDecimal price,
    Integer stockQuantity,
    String description,
    ProductType productType,
    List<String> tags,
    List<ProductVariationResponseDTO> variation,
    LocalDateTime deletedAt
) {
    
    public static ProductResponseDTO from(Product product) {
        List<String> tags = product.getTags()
            .stream()
            .map(ProductTag -> ProductTag.getName())
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

            List<ProductVariationResponseDTO> variations = product.getVariations()
                .stream()
                .map(ProductVariationResponseDTO::from)
                .toList();

                return new ProductResponseDTO(
                    product.getId(),
                    product.getName(),
                    product.getPrice(), 
                    product.getStockQuantity(), 
                    product.getDescription() , 
                    product.getProductType(), 
                    tags, 
                    variations, 
                    product.getDeletedAt());
    }



}
