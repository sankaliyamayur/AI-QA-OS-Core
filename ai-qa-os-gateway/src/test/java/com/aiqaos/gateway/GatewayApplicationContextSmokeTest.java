package com.aiqaos.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.gateway.controller.WorkflowController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;

/**
 * ADR-089: proves {@link GatewayApplication} actually assembles.
 *
 * <p>The dashboard spent two days unbootable on {@code main} behind a fully green CI, because a
 * component-scan conflict is invisible to slice tests and nothing started the real context. The
 * gateway carries the same risk — a wholesale {@code com.aiqaos} scan over a large module graph —
 * and had no context test either. This is the counterpart of
 * {@code DashboardApplicationContextSmokeTest}; the two are separate because a scan conflict is a
 * property of one application's own classpath, so neither can stand in for the other.
 *
 * <p>Boots against in-memory H2 with Flyway off and security disabled: the subject is bean assembly.
 * Migrations, real authentication and anything needing Postgres are covered by the live E2E runbook.
 */
@SpringBootTest(
        classes = GatewayApplication.class,
        // RANDOM_PORT, not MOCK and not NONE. NONE fails outright — a servlet app whose security chain
        // needs the servlet context (see the dashboard counterpart, which failed on
        // mvcHandlerMappingIntrospector). MOCK was enough while this class only inspected beans, but
        // theGeneratedOpenApiSpecIsServed makes a real HTTP request, so Tomcat has to be up.
        //
        // That test lives here rather than in its own class on purpose: TelemetryConfig calls
        // GlobalOpenTelemetry.set, a JVM-wide singleton that throws "has already been called" on a
        // second context. Surefire forks per module, so the gateway module gets exactly one
        // application context — a separate @SpringBootTest class here would fail on OTel, not on
        // anything it meant to assert.
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:gateway-smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                // The gateway owns the canonical migrations (ADR-024) and normally runs ddl-auto:
                // validate against them. Here Hibernate creates the schema instead — this test is
                // about beans, and V1..V24 against H2 is a different (and lower-value) test.
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.flyway.enabled=false",
                "aiqaos.security.enabled=false"
        })
class GatewayApplicationContextSmokeTest {

    @Autowired
    private ApplicationContext context;

    @LocalServerPort
    private int port;

    /**
     * Proves the OpenAPI spec is actually served, not merely that springdoc is on the classpath.
     *
     * <p>springdoc's major version tracks the Spring generation (2.x = Boot 3, 3.x = Boot 4). During
     * the Boot 4 upgrade the build stayed green on springdoc 2.5.0 — all 634 tests passing — while
     * {@code /v3/api-docs} returned HTTP 500, {@code NoSuchMethodError} on Framework 7's
     * {@code ControllerAdviceBean}. Nothing caught it because nothing requested the endpoint.
     *
     * <p>{@code /swagger-ui/index.html} is deliberately not the assertion: it is a static page and
     * served a healthy 200 throughout that outage. Only the generated spec proves the integration.
     */
    @Test
    void theGeneratedOpenApiSpecIsServed() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "springdoc failed to generate the spec — usually a springdoc major that does not match "
                        + "the Spring generation. Body: " + response.body());
        assertTrue(response.body().contains("\"openapi\""),
                "expected an OpenAPI document, got: " + response.body());
        assertTrue(response.body().contains("\"paths\""),
                "the spec must describe the gateway's endpoints, got: " + response.body());
    }

    /** Fails before the body runs if the scan conflicts or any bean cannot be satisfied. */
    @Test
    void theApplicationContextStarts() {
        assertNotNull(context);
        assertTrue(context.getBeanDefinitionCount() > 0);
    }

    @Test
    void theScanReachesThisApplicationsOwnControllers() {
        assertNotNull(context.getBean(WorkflowController.class));
    }

    /**
     * Cross-module simple-name collisions already resolved with explicit qualified names — the
     * convention this codebase follows, e.g. {@code @Component("observabilityWorkflowMetricsCollector")}.
     * The gateway's classpath differs from the dashboard's, so its inventory differs too.
     */
    private static final Set<String> KNOWN_QUALIFIED_COLLISIONS = Set.of(
            "SlackNotificationSender",
            "TeamsNotificationSender",
            "WorkflowMetricsCollector",
            "ExecutionMetricsCollector",
            "DashboardService",
            "SecurityMetricsCollector");

    /** See the dashboard counterpart: early warning before an unqualified collision breaks startup. */
    @Test
    void anyNewCrossModuleNameCollisionMustBeExplicitlyQualified() {
        Map<String, Set<String>> bySimpleName = new HashMap<>();
        for (String name : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(name);
            if (type == null || !type.getName().startsWith("com.aiqaos")) {
                continue;
            }
            bySimpleName.computeIfAbsent(type.getSimpleName(), k -> new HashSet<>()).add(type.getName());
        }

        String unexpected = bySimpleName.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .filter(e -> !KNOWN_QUALIFIED_COLLISIONS.contains(e.getKey()))
                .map(e -> e.getKey() + " -> " + new java.util.TreeSet<>(e.getValue()))
                .collect(Collectors.joining("; "));

        assertTrue(unexpected.isEmpty(),
                "a new cross-module class-name collision appeared on the gateway's classpath. Give "
                        + "each bean an explicit qualified name — @Component(\"<module>SomeClass\") — "
                        + "then add it to KNOWN_QUALIFIED_COLLISIONS: " + unexpected);
    }
}
