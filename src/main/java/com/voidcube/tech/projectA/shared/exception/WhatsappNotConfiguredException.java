package com.voidcube.tech.projectA.shared.exception;

public class WhatsappNotConfiguredException extends RuntimeException {
    
    public WhatsappNotConfiguredException(String domainUrl) {
        super("A landing page " + domainUrl + " não possui um número de whatsapp configurado.");
    }
}
