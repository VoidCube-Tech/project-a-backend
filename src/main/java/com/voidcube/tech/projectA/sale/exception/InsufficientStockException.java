package com.voidcube.tech.projectA.sale.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(
            Long productId,
            Long variationId
    ) {
        super(buildMessage(productId, variationId));
    }

    private static String buildMessage(
            Long productId,
            Long variationId
    ) {
        if (variationId == null) {
            return "Estoque insuficiente para o produto "
                    + productId + ".";
        }

        return "Estoque insuficiente para a variação "
                + variationId + " do produto " + productId + ".";
    }
}