package com.voidcube.tech.projectA.shared.exception;

public class InvalidProductException extends RuntimeException  {
    
    public InvalidProductException(String message) {
        super(message);
    }
}
