package com.voidcube.tech.projectA.promotion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("PERCENTAGE")
public class PercentagePromotion extends Promotion {
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    public PercentagePromotion() {
        super(PromotionType.PERCENTAGE);
    }

    @Override
    public BigDecimal calculatePriceWithDiscount(BigDecimal originalPrice) {
        BigDecimal validPrice = validateOriginalPrice(originalPrice);

        if(!isActive()) {
            return validPrice;
        }

        if(discountPercentage == null) {
            throw new IllegalStateException("A porcentagem de desconto não foi configurada");
        }

        if(discountPercentage.signum() < 0 || discountPercentage.compareTo(new BigDecimal("100"))> 0) {
            throw new IllegalStateException("Percentual deve estar entre 0 e 100");
        }

        BigDecimal percentageAsDecimal = discountPercentage.movePointLeft(2);
        BigDecimal discount = validPrice.multiply(percentageAsDecimal);

        return validPrice
            .subtract(discount)
            .setScale(2, RoundingMode.HALF_UP);
}
}
