package com.aiqaos.core.exception;

public enum ErrorCode {
    SYSTEM_ERROR("SYS_001", "An internal system error occurred"),
    VALIDATION_ERROR("VAL_001", "Validation failed"),
    UNAUTHORIZED("AUTH_001", "Unauthorized access"),
    FORBIDDEN("AUTH_002", "Access forbidden"),
    NOT_FOUND("RES_001", "Resource not found"),
    BUSINESS_ERROR("BIZ_001", "Business rule violation");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}