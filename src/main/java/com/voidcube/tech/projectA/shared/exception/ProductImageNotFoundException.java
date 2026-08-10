package com.voidcube.tech.projectA.shared.exception;

public class ProductImageNotFoundException extends RuntimeException {
    
    public ProductImageNotFoundException(Long imageId) {
        super("Imagem do produto não encontrada: " + imageId);
    }
}
