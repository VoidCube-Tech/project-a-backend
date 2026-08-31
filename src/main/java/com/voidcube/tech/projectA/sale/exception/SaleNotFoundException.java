package com.voidcube.tech.projectA.sale.exception;

public class SaleNotFoundException extends RuntimeException {

    public SaleNotFoundException(Long saleId) {
        super("Venda não encontrada: " + saleId);
    }
}