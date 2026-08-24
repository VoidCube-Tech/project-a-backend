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

    @NotBlank(message = "{validation.promotion.name.required}")
    @Size(max = 255, message = "{validation.promotion.name.size}")
    String name,

    @NotNull(message = "{validation.promotion.active.required}")
    Boolean active,

    @NotNull(message = "{validation.promotion.type.required}")
    PromotionType promotionType,

    @DecimalMin(value = "0.01", message = "{validation.promotion.percentage.minimum}")
    @DecimalMax(value = "100.00", message = "{validation.promotion.percentage.maximum}")
    @Digits(integer = 3, fraction = 2, message = "{validation.promotion.percentage.digits}")
    BigDecimal discountPercentage,

    LocalDateTime startDate,

    LocalDateTime endDate,

    @DecimalMin(value = "0.01", message = "{validation.promotion.value.minimum}")
    @Digits(integer = 17, fraction = 2, message = "{validation.promotion.value.digits}")
    BigDecimal discountValue,

    @Size(max = 100, message = "{validation.promotion.coupon.size}")
    String couponCode,

    @Positive(message = "{validation.promotion.usage-limit.positive}")
    Integer usageLimit

) {}