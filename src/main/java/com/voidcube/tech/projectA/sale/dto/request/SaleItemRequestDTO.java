package com.voidcube.tech.projectA.sale.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleItemRequestDTO(

        @NotNull(message = "{validation.sale.product-id.required}")
        @Positive(message = "{validation.sale.product-id.positive}")
        Long productId,

        @Positive(message = "{validation.sale.variation-id.positive}")
        Long variationId,

        @NotNull(message = "{validation.sale.quantity.required}")
        @Positive(message = "{validation.sale.quantity.positive}")
        Integer quantity,

        @NotNull(message = "{validation.sale.unit-price.required}")
        @DecimalMin(value = "0.00", message = "{validation.sale.unit-price.minimum}")
        @Digits(integer = 17,fraction = 2, message = "{validation.sale.unit-price.digits}")
        BigDecimal unitPrice
) {
    
}
