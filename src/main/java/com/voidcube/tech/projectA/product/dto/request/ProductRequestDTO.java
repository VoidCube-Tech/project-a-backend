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

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 255, message = "O nome do produto não pode exceder 255 caracteres")
    String name,

    @NotNull(message = "O preço do produto é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "O preço não pode ser negativo.")
    @Digits(integer = 17, fraction = 2)
    BigDecimal price,

    @Size(max = 10000, message = "A descrição deve possuir no máximo 10000 caracteres")
    String description,

    @NotNull(message = "O tipo do produto é obrigatório")
    ProductType productType,

    @PositiveOrZero(message = "O estoque do produto não pode ser negativo")
    Integer stockQuantity,

    List<@NotBlank(message = "O nome da tag não pode estar vazia.")@Size(max = 255, message = "O nome da tag não deve exceder 255 caracteres") String> tags,
    List<@Valid ProductVariationRequestDTO> variations



) {}
