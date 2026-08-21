package com.voidcube.tech.projectA.shared.exception;

public record FieldValidationErrorResponse(
    String field,
    String message
) {
    
}
