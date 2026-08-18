package com.voidcube.tech.projectA.shared.exception;

public class PlanNotFoundException extends RuntimeException {
    
    public PlanNotFoundException(Long planId) {
        super("Plano não encontrado: " + planId);
    }
}
