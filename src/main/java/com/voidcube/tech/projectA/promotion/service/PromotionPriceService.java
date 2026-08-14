package com.voidcube.tech.projectA.promotion.service;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.promotion.model.Promotion;
import com.voidcube.tech.projectA.promotion.model.PromotionType;

@Service
public class PromotionPriceService {
    
    public BigDecimal calculateFinalPrice(Product product) {
        if(product == null) {
            throw new IllegalStateException("O produto não pode ser nulo");
        }

        BigDecimal originalPrice = Objects.requireNonNull(product.getPrice(), "O preço do produto não pode ser nulo");

        if(originalPrice.signum() < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo");
        }

        return product.getPromotions()
            .stream()
            .filter(Objects::nonNull)
            .filter(promotion -> promotion.isActive())
            .filter(this::isAutomaticPromotion)
            .map(promotion -> promotion.calculatePriceWithDiscount(originalPrice))
            .min((firstPrice, secondPrice) -> firstPrice.compareTo(originalPrice))
            .orElse(originalPrice);
    }




    private boolean isAutomaticPromotion(Promotion promotion) {
        return promotion.getPromotionType() == PromotionType.PERCENTAGE || promotion.getPromotionType() == PromotionType.SCHEDULED;
    }
}
