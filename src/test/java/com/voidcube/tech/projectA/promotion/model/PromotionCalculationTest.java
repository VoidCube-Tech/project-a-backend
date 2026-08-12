package com.voidcube.tech.projectA.promotion.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromotionCalculationTest {

    @Test
    void shouldApplyPercentageDiscount() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setActive(true);
        promotion.setDiscountPercentage(
                new BigDecimal("15.00")
        );

        BigDecimal result =
                promotion.calculatePriceWithDiscount(
                        new BigDecimal("200.00")
                );

        assertEquals(
                new BigDecimal("170.00"),
                result
        );
    }

    @Test
    void shouldNotApplyInactivePromotion() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setActive(false);
        promotion.setDiscountPercentage(
                new BigDecimal("15.00")
        );

        BigDecimal result =
                promotion.calculatePriceWithDiscount(
                        new BigDecimal("200.00")
                );

        assertEquals(
                new BigDecimal("200.00"),
                result
        );
    }

    @Test
    void shouldApplyScheduledDiscountInsidePeriod() {
        ScheduledPromotion promotion =
                new ScheduledPromotion();

        promotion.setActive(true);
        promotion.setStartDate(
                LocalDateTime.now().minusDays(1)
        );
        promotion.setEndDate(
                LocalDateTime.now().plusDays(1)
        );
        promotion.setDiscountValue(
                new BigDecimal("25.00")
        );

        BigDecimal result =
                promotion.calculatePriceWithDiscount(
                        new BigDecimal("100.00")
                );

        assertEquals(
                new BigDecimal("75.00"),
                result
        );
    }

    @Test
    void shouldNeverReturnNegativePrice() {
        CouponPromotion promotion =
                new CouponPromotion();

        promotion.setActive(true);
        promotion.setCouponCode("TESTE");
        promotion.setUsageLimit(10);
        promotion.setDiscountValue(
                new BigDecimal("150.00")
        );

        BigDecimal result =
                promotion.calculatePriceWithDiscount(
                        new BigDecimal("100.00")
                );

        assertEquals(
                BigDecimal.ZERO,
                result
        );
    }

    @Test
    void shouldRejectNegativeOriginalPrice() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        assertThrows(
                IllegalArgumentException.class,
                () -> promotion.calculatePriceWithDiscount(
                        new BigDecimal("-10.00")
                )
        );
    }
}
