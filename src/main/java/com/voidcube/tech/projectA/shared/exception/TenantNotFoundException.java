package com.voidcube.tech.projectA.shared.exception;

public class TenantNotFoundException extends RuntimeException {
    
    public TenantNotFoundException(Long tenantId) {
        super("Tenant não encontrado: " + tenantId);
    }
}
