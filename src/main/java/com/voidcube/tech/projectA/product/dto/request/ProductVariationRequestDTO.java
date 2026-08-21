package com.voidcube.tech.projectA.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductVariationRequestDTO(

    @NotBlank(message = "O nome da variação é obrigatório.")
    @Size(max = 255 ,message = "nome da variação deve ter no máximo 255 caracteres")
    String variationName,

    @NotBlank(message = "o valor da variação é obrigatório.")
    @Size(max = 255, message = "O valor da variação deve ter no máximo 255 caracteres")
    String variationValue,

    @NotNull(message = "O estoque da variação é obrigatório")
    @PositiveOrZero(message = "O estoque não pode ser negativo")
    Integer stockQuantity   
) 
{}
