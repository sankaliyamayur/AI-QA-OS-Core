package com.aiqaos.core.exception;

public class SystemException extends BaseException {
    public SystemException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }

    public SystemException(String messagePattern, Object... args) {
        super(ErrorCode.SYSTEM_ERROR, messagePattern, args);
    }
}