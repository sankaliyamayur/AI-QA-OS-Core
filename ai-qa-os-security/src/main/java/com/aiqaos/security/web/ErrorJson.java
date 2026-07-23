package com.aiqaos.security.web;

import java.time.Instant;

/**
 * SEC-1 — minimal, dependency-free JSON error envelope shared by the 401/403 handlers.
 * Kept deliberately tiny (no ObjectMapper wiring) so the handlers stay simple and allocation-light.
 */
final class ErrorJson {

    private ErrorJson() {
    }

    static String body(int status, String error, String message, String path) {
        return "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + escape(error) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(path) + "\""
                + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
