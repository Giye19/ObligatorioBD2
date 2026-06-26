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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", errors);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return buildResponse(ex.getStatus(), "Error de negocio", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(
            org.springframework.dao.DataAccessException ex) {

        Throwable causaRaiz = ex.getRootCause();

        if (causaRaiz instanceof java.sql.SQLException sqlEx) {

            if ("45000".equals(sqlEx.getSQLState())) {
                return buildResponse(HttpStatus.CONFLICT, "Regla de negocio violada", sqlEx.getMessage());
            }

            if ("23000".equals(sqlEx.getSQLState())) {
                return buildResponse(
                        HttpStatus.CONFLICT,
                        "Registro duplicado",
                        "Ya existe un registro con esos mismos datos");
            }
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error de base de datos",
                "Ocurrió un error al procesar la operación en la base de datos");
    }

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