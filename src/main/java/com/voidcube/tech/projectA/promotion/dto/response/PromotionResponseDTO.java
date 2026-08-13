package com.voidcube.tech.projectA.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.voidcube.tech.projectA.promotion.model.CouponPromotion;
import com.voidcube.tech.projectA.promotion.model.PercentagePromotion;
import com.voidcube.tech.projectA.promotion.model.Promotion;
import com.voidcube.tech.projectA.promotion.model.PromotionType;
import com.voidcube.tech.projectA.promotion.model.ScheduledPromotion;

public record PromotionResponseDTO(
    Long id,
    String name,
    boolean active,
    PromotionType promotionType,
    BigDecimal discountPercentage,
    LocalDateTime startDate,
    LocalDateTime endDate,
    BigDecimal discountValue,
    String couponCode,
    Integer usageLimit
) {

    public static PromotionResponseDTO from(Promotion promotion) {
        if (promotion instanceof PercentagePromotion percentagePromotion) {
            return new PromotionResponseDTO(
                promotion.getId(),
                promotion.getName(),
                promotion.isActive(),
                PromotionType.PERCENTAGE,
                percentagePromotion.getDiscountPercentage(),
                null,
                null,
                null,
                null,
                null
            );
        }

        if (promotion instanceof ScheduledPromotion scheduledPromotion) {
            return new PromotionResponseDTO(
                promotion.getId(),
                promotion.getName(),
                promotion.isActive(),
                PromotionType.SCHEDULED,
                null,
                scheduledPromotion.getStartDate(),
                scheduledPromotion.getEndDate(),
                scheduledPromotion.getDiscountValue(),
                null,
                null
            );
        }

        if (promotion instanceof CouponPromotion couponPromotion) {
            return new PromotionResponseDTO(
                promotion.getId(),
                promotion.getName(),
                promotion.isActive(),
                PromotionType.COUPON,
                null,
                null,
                null,
                couponPromotion.getDiscountValue(),
                couponPromotion.getCouponCode(),
                couponPromotion.getUsageLimit()
            );
        }

        throw new IllegalArgumentException(
            "Tipo de promoção não suportado: "
                + promotion.getClass().getName()
        );
    }
}
