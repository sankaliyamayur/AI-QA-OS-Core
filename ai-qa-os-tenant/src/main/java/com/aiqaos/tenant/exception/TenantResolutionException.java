package com.aiqaos.tenant.exception;

/**
 * MOD-1: thrown when a tenant key cannot be resolved to a usable context — the tenant is unknown or
 * not {@code ACTIVE}. Resides in {@code tenant.exception} per the platform's exception-package
 * convention.
 */
public class TenantResolutionException extends RuntimeException {

    public TenantResolutionException(String message) {
        super(message);
    }
}
