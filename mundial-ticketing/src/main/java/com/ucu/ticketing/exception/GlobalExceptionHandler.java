package com.ucu.ticketing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejo global de excepciones para toda la API.
 *
 * En vez de que cada Controller tenga try/catch, todas las
 * excepciones se centralizan acá. Devuelve siempre JSON con:
 *   - timestamp
 *   - status (código HTTP)
 *   - error (descripción)
 *   - message (detalle)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de validación de campos en DTOs (@Valid).
     * Devuelve 400 con el detalle de qué campo falló.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", errors);
    }

    /**
     * Errores de lógica de negocio (reglas del dominio).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return buildResponse(ex.getStatus(), "Error de negocio", ex.getMessage());
    }

    /**
     * Cualquier otra excepción no contemplada.
     * Devuelve 500 para no exponer detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String error, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
