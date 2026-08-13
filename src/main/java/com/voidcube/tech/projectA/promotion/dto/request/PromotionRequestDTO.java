package com.voidcube.tech.projectA.promotion.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.voidcube.tech.projectA.promotion.model.PromotionType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PromotionRequestDTO(

    @NotBlank(message = "O nome da promoção é obrigatório")
    @Size(max = 255, message = "O nome da promoção não pode exceder 255 caracteres")
    String name,

    @NotNull(message = "O estado da promoção é obrigatório")
    Boolean active,

    @NotNull(message = "O tipo da promoção é obrigatório")
    PromotionType promotionType,

    @DecimalMin(value = "0.01", message = "O percentual de desconto deve ser maior que zero")
    @DecimalMax(value = "100.00", message = "O percentual de desconto não pode exceder 100")
    @Digits(integer = 3, fraction = 2, message = "O percentual de desconto deve possuir no máximo 2 casas decimais")
    BigDecimal discountPercentage,

    LocalDateTime startDate,

    LocalDateTime endDate,

    @DecimalMin(value = "0.01", message = "O valor do desconto deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "O valor do desconto deve possuir no máximo 2 casas decimais")
    BigDecimal discountValue,

    @Size(max = 100, message = "O código do cupom não pode exceder 100 caracteres")
    String couponCode,

    @Positive(message = "O limite de uso deve ser maior que zero")
    Integer usageLimit
) {}
