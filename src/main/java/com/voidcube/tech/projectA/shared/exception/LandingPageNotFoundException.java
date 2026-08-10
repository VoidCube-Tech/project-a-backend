package com.voidcube.tech.projectA.shared.exception;

public class LandingPageNotFoundException extends RuntimeException {
    
    public LandingPageNotFoundException(Long id) {
        super("Landing page não encontrada" + id);
    }
}
