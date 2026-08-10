package com.aiqaos.gateway;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.gateway.controller.WorkflowController;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        // MOCK, not NONE — a servlet app whose security chain needs the servlet context (see the
        // dashboard counterpart, where NONE failed on mvcHandlerMappingIntrospector).
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
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
