package com.voidcube.tech.projectA.export.exception;

public class InvalidExportFormatException extends RuntimeException {
    
    public InvalidExportFormatException(String format) {
        super("Formato de exportação inválida" + format + ". utilizar json ou csv.");
    }
}
