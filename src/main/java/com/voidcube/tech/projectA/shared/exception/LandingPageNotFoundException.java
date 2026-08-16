package com.voidcube.tech.projectA.shared.exception;

public class LandingPageNotFoundException extends RuntimeException {
    
    public LandingPageNotFoundException(Long id) {
        super("Landing page não encontrada: " + id);
    }

    public LandingPageNotFoundException(String message) {
        super(message);
    }

    public static LandingPageNotFoundException byDomainUrl(String domainUrl) {
        return new LandingPageNotFoundException("Landing page não encontrada para o domainUrl: "+ domainUrl);
    }
    
}
