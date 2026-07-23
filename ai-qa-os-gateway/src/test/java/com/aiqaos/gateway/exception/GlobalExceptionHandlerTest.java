package com.aiqaos.gateway.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * MNT-3 gateway coverage — the error contract. A {@link GatewayException} maps to its own status
 * and error code; any other exception maps to a generic 500 without leaking a specific type.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsGatewayExceptionToItsStatusAndErrorCode() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGatewayException(new GatewayException("no such workflow", 404, "NOT_FOUND"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody())
                .containsEntry("status", 404)
                .containsEntry("error", "NOT_FOUND")
                .containsEntry("message", "no such workflow");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void mapsUnexpectedExceptionToGeneric500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody())
                .containsEntry("status", 500)
                .containsEntry("error", "INTERNAL_ERROR");
    }
}
