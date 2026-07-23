package com.aiqaos.security.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-1 — verifies the REST 401/403 handlers emit the correct status and a JSON body,
 * rather than a login redirect or HTML error page.
 */
class RestErrorHandlersTest {

    @Test
    void entryPointReturns401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workflows/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RestAuthenticationEntryPoint().commence(request, response, new AuthenticationException("no token") {});

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("\"error\":\"Unauthorized\"")
                .contains("\"path\":\"/api/v1/workflows/123\"");
    }

    @Test
    void accessDeniedHandlerReturns403Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/dashboard/modules");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RestAccessDeniedHandler().handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("\"status\":403")
                .contains("\"error\":\"Forbidden\"")
                .contains("\"path\":\"/api/dashboard/modules\"");
    }
}
