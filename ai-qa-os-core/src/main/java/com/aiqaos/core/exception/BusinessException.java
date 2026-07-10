package com.aiqaos.core.exception;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(String messagePattern, Object... args) {
        super(ErrorCode.BUSINESS_ERROR, messagePattern, args);
    }
}