package com.aiqaos.core.exception;

public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object details;

    public BaseException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String messagePattern, Object... args) {
        super(messagePattern != null ? String.format(messagePattern, args) : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String message, Object details) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public BaseException(ErrorCode errorCode, Object details, String messagePattern, Object... args) {
        super(messagePattern != null ? String.format(messagePattern, args) : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public Object getDetails() { return details; }
}