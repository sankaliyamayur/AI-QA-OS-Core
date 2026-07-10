package com.aiqaos.core.exception;

public class ValidationException extends BaseException {
    public ValidationException(String message, Object details) {
        super(ErrorCode.VALIDATION_ERROR, message, details);
    }

    public ValidationException(String messagePattern, Object... args) {
        super(ErrorCode.VALIDATION_ERROR, messagePattern, args);
    }

    public ValidationException(Object details, String messagePattern, Object... args) {
        super(ErrorCode.VALIDATION_ERROR, details, messagePattern, args);
    }
}