package com.aiqaos.provider.exception;

public class ProviderException extends RuntimeException {
    public ProviderException(String msg) {
        super(msg);
    }
    public ProviderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}