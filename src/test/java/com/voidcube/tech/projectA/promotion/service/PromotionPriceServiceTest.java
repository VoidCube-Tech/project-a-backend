package com.voidcube.tech.projectA.promotion.service;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.promotion.model.CouponPromotion;
import com.voidcube.tech.projectA.promotion.model.PercentagePromotion;
import com.voidcube.tech.projectA.promotion.model.ScheduledPromotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromotionPriceServiceTest {

    private PromotionPriceService promotionPriceService;
    private Product product;

    @BeforeEach
    void setUp() {
        promotionPriceService =
                new PromotionPriceService();

        product = new Product();
        product.setPrice(
                new BigDecimal("100.00")
        );
    }

    @Test
    void shouldReturnOriginalPriceWithoutPromotions() {
        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("100.00"),
                finalPrice
        );
    }

    @Test
    void shouldApplyPercentagePromotion() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setActive(true);
        promotion.setDiscountPercentage(
                new BigDecimal("10.00")
        );

        promotion.addProduct(product);

        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("90.00"),
                finalPrice
        );
    }

    @Test
    void shouldApplyScheduledPromotionInsidePeriod() {
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
                new BigDecimal("20.00")
        );

        promotion.addProduct(product);

        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("80.00"),
                finalPrice
        );
    }

    @Test
    void shouldIgnoreInactivePromotion() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setActive(false);
        promotion.setDiscountPercentage(
                new BigDecimal("50.00")
        );

        promotion.addProduct(product);

        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("100.00"),
                finalPrice
        );
    }

    @Test
    void shouldIgnoreCouponInAutomaticPrice() {
        CouponPromotion coupon =
                new CouponPromotion();

        coupon.setActive(true);
        coupon.setCouponCode("SAVE50");
        coupon.setDiscountValue(
                new BigDecimal("50.00")
        );
        coupon.setUsageLimit(10);

        coupon.addProduct(product);

        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("100.00"),
                finalPrice
        );
    }

    @Test
    void shouldChoosePromotionWithLowestFinalPrice() {
        PercentagePromotion percentage =
                new PercentagePromotion();

        percentage.setActive(true);
        percentage.setDiscountPercentage(
                new BigDecimal("10.00")
        );

        ScheduledPromotion scheduled =
                new ScheduledPromotion();

        scheduled.setActive(true);
        scheduled.setStartDate(
                LocalDateTime.now().minusDays(1)
        );
        scheduled.setEndDate(
                LocalDateTime.now().plusDays(1)
        );
        scheduled.setDiscountValue(
                new BigDecimal("25.00")
        );

        percentage.addProduct(product);
        scheduled.addProduct(product);

        BigDecimal finalPrice =
                promotionPriceService
                        .calculateFinalPrice(product);

        assertEquals(
                new BigDecimal("75.00"),
                finalPrice
        );
    }
}
