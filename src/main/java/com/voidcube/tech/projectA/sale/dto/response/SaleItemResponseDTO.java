package com.voidcube.tech.projectA.sale.dto.response;

import java.math.BigDecimal;

import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.sale.model.SaleItem;

public record SaleItemResponseDTO(
    Long id,
    Long productId,
    Long variationId,
    String productName,
    String variationDescription,
    ProductType productType,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal subtotal
) {
   
    public static SaleItemResponseDTO from(SaleItem item) {
        return new SaleItemResponseDTO(
            item.getId(),
            item.getProductId(),
            item.getVariationId(),
            item.getProductName(),
            item.getVariationDescription(),
            item.getProductType(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getSubtotal()
        );
    }
}
