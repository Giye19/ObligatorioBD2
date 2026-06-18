package com.ucu.ticketing.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de lógica de negocio.
 *
 * Se lanza desde los Services cuando se viola una regla del dominio.
 * El GlobalExceptionHandler la captura y devuelve el código HTTP adecuado.
 *
 * Ejemplos de uso:
 *   throw new BusinessException("No se puede comprar más de 5 entradas", HttpStatus.BAD_REQUEST);
 *   throw new BusinessException("Entrada ya consumida", HttpStatus.CONFLICT);
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
