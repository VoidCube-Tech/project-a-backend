package com.voidcube.tech.projectA.promotion.exception;

public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException(Long promotionId) {
        super("Promoção não encontrada: " + promotionId);
    }
}
