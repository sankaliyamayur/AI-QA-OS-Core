package com.aiqaos.gateway.exception;

public class AuthorizationException extends GatewayException {
    public AuthorizationException(String message) {
        super(message, 403, "ACCESS_DENIED");
    }
}