package com.aiqaos.core.exception;

public class AuthenticationException extends BaseException {
    public AuthenticationException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(String messagePattern, Object... args) {
        super(ErrorCode.UNAUTHORIZED, messagePattern, args);
    }
}