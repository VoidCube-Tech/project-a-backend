package com.voidcube.tech.projectA.promotion.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("COUPON")
@Getter
@Setter
public class CouponPromotion extends Promotion {
    
    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "coupon_discount_value", precision = 19 , scale = 2)
    private BigDecimal discountValue;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Override
    public BigDecimal calculatePriceWithDiscount(BigDecimal originalPrice) {

        BigDecimal validPrice = validateOriginalPrice(originalPrice);

        if(!isActive()) {
            return validPrice;
        }

        if(couponCode == null || couponCode.isBlank()) {
            throw new IllegalStateException("O código do cupom não foi inserido");
        }

        if(usageLimit == null || usageLimit <= 0) {
            throw new IllegalStateException("O limite de uso deve ser maior que zero");
        }

        return applyFixedDiscount(validPrice, discountValue);
    }

    public CouponPromotion() {
        super(PromotionType.COUPON);
    }
}
