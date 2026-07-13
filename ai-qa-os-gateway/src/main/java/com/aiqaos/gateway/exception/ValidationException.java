package com.aiqaos.gateway.exception;

public class ValidationException extends GatewayException {
    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR");
    }
}