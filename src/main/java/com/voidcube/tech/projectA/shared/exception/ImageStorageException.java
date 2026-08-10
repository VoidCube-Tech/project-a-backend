package com.voidcube.tech.projectA.shared.exception;

public class ImageStorageException extends RuntimeException  {
    
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageStorageException(String message) {
        super(message);
    }
}
