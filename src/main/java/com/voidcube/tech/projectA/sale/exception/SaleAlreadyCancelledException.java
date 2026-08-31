package com.voidcube.tech.projectA.sale.exception;

public class SaleAlreadyCancelledException
        extends RuntimeException {

    public SaleAlreadyCancelledException(Long saleId) {
        super("A venda " + saleId + " já foi cancelada.");
    }
}