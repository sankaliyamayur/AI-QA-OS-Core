package com.aiqaos.core.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public NotFoundException(String messagePattern, Object... args) {
        super(ErrorCode.NOT_FOUND, messagePattern, args);
    }
}