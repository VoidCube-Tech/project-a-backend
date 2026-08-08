package com.voidcube.tech.projectA.shared.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(Long productId) {
        super("Produto não encontrado" + productId);
    }
}
