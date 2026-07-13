package com.aiqaos.gateway.exception;

public class GatewayException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public GatewayException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
}