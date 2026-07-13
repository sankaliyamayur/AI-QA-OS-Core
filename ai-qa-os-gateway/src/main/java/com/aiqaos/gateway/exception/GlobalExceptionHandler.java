package com.aiqaos.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayException(GatewayException ex) {
        return buildResponse(ex.getStatusCode(), ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(500, "INTERNAL_ERROR", ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(int status, String error,
                                                              String message, String correlationId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        if (correlationId != null) body.put("correlationId", correlationId);
        return ResponseEntity.status(status).body(body);
    }
}