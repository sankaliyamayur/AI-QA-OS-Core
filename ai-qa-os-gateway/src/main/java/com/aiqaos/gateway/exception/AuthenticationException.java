package com.aiqaos.gateway.exception;

public class AuthenticationException extends GatewayException {
    public AuthenticationException(String message) {
        super(message, 401, "AUTHENTICATION_FAILED");
    }
}