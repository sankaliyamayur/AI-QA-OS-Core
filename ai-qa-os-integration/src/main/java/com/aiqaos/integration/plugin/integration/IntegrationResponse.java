package com.aiqaos.integration.plugin.integration;

/**
 * PLG-2: the outcome of an integration action — whether it succeeded, a message, and any returned
 * data.
 */
public final class IntegrationResponse {

    private final boolean success;
    private final String message;
    private final String data;

    public IntegrationResponse(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static IntegrationResponse ok(String message) {
        return new IntegrationResponse(true, message, null);
    }

    public static IntegrationResponse ok(String message, String data) {
        return new IntegrationResponse(true, message, data);
    }

    public static IntegrationResponse fail(String message) {
        return new IntegrationResponse(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getData() { return data; }
}
