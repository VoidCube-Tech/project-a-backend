package com.voidcube.tech.projectA.product.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.voidcube.tech.projectA.product.model.ProductType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO(

    @NotBlank(message = "{validation.product.name.required}")
    @Size(max = 255, message = "{validation.product.name.size}")
    String name,

    @NotNull(message = "{validation.product.price.required}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{validation.product.price.minimum}")
    @Digits(integer = 17, fraction = 2, message = "{validation.product.price.digits}")
    BigDecimal price,

    @Size(max = 10000, message = "{validation.product.description.size}")
    String description,

    @NotNull(message = "{validation.product.type.required}")
    ProductType productType,

    @PositiveOrZero(message = "{validation.product.stock.non-negative}}")
    Integer stockQuantity,

    List<@NotBlank(message = "{validation.product.tag.name.required}")@Size(max = 255, message = "{validation.product.tag.name.size}") 
    String> tags,

    List< @Valid ProductVariationRequestDTO> variations



) {}
