package com.aiqaos.core.exception;

public class AuthorizationException extends BaseException {
    public AuthorizationException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public AuthorizationException(String messagePattern, Object... args) {
        super(ErrorCode.FORBIDDEN, messagePattern, args);
    }
}