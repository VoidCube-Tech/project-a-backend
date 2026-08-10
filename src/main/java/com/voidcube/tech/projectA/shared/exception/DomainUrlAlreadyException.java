package com.voidcube.tech.projectA.shared.exception;

public class DomainUrlAlreadyException extends RuntimeException {
    
    public DomainUrlAlreadyException(String domainUrl) {
        super("O domínio informado já está sendo utilizado" + domainUrl);
    }
}