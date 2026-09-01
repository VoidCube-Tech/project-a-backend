package com.voidcube.tech.projectA.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductVariationRequestDTO(
        @NotBlank(message = "{validation.product.variation.name.required}")
        @Size(max = 255, message = "{validation.product.variation.name.size}")
        String variationName,

        @NotBlank(message = "{validation.product.variation.value.required}")
        @Size(max = 255, message = "{validation.product.variation.value.size}")
        String variationValue,

        @NotNull(message = "{validation.product.variation.stock.required}")
        @PositiveOrZero(
                message = "{validation.product.variation.stock.non-negative}"
        )
        Integer stockQuantity
) {
}