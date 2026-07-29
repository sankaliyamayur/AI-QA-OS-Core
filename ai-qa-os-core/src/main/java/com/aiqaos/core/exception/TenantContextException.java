package com.aiqaos.core.exception;

/**
 * ENT-1: thrown by {@code TenantContextHolder.require()} when code that must run tenant-scoped finds
 * no tenant bound to the current thread. Resides in {@code core.exception} per the platform's
 * exception-package convention.
 */
public class TenantContextException extends RuntimeException {

    public TenantContextException(String message) {
        super(message);
    }
}
