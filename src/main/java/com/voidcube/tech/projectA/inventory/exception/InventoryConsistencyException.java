package com.voidcube.tech.projectA.inventory.exception;

public class InventoryConsistencyException extends RuntimeException {

    public InventoryConsistencyException(Long saleId) {
        super("Não foi possível restaurar o estoque da venda " + saleId + " por inconsistência nos produtos.");
    }
}