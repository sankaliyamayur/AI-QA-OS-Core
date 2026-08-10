package com.aiqaos.dashboard;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.dashboard.controller.LearningController;
import com.aiqaos.dashboard.controller.ReviewController;
import java.util.Arrays;
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
 * ADR-089: proves {@link DashboardApplication} actually assembles.
 *
 * <p><b>Why this exists.</b> Every other test in this module is a {@code @WebMvcTest} slice, which
 * instantiates one controller and never runs the application's component scan. On 2026-08-08 the
 * dashboard became unbootable — two modules contributed a {@code @Component} named
 * {@code SlackNotificationSender}, so the {@code com.aiqaos}-wide scan raised
 * {@code ConflictingBeanDefinitionException} — and CI stayed green for two days, because nothing
 * anywhere started this context.
 *
 * <p><b>Why the existing integration tests could not have caught it.</b> {@code TestApplication} in
 * {@code ai-qa-os-integration} does scan {@code com.aiqaos} wholesale, but that module's classpath
 * carries {@code ai-qa-os-reporting} and not {@code ai-qa-os-notification}, so only one of the
 * clashing classes is ever present there. A scan conflict is a property of <i>an application's own
 * classpath</i>, which is why this test has to live in the application's module.
 *
 * <p>Boots against in-memory H2 with Flyway off: the point is bean assembly, not schema. Anything
 * that needs a real Postgres belongs in the live E2E runbook instead.
 */
@SpringBootTest(
        classes = DashboardApplication.class,
        // MOCK, not NONE: this is a servlet application, and its security chain uses MVC request
        // matchers that need the servlet context's mvcHandlerMappingIntrospector. NONE would both
        // fail to start and test a shape the app never runs in. MOCK builds the full web context
        // without binding a port.
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:dashboard-smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.flyway.enabled=false",
                "aiqaos.security.enabled=false"
        })
class DashboardApplicationContextSmokeTest {

    @Autowired
    private ApplicationContext context;

    /**
     * The assertion is that {@code @Autowired} above resolved at all — if the scan conflicts or a
     * bean cannot be satisfied, this test fails before its body runs. That is the whole point.
     */
    @Test
    void theApplicationContextStarts() {
        assertNotNull(context);
        assertTrue(context.getBeanDefinitionCount() > 0);
    }

    @Test
    void theScanReachesThisApplicationsOwnControllers() {
        // Guards against the context "starting" because scanning silently covered nothing.
        assertNotNull(context.getBean(ReviewController.class));
        assertNotNull(context.getBean(LearningController.class));
    }

    /**
     * Cross-module simple-name collisions that are known and already resolved by giving each bean an
     * explicit qualified name. This list is the inventory of a real hazard, not a suppression: the
     * codebase's convention is {@code @Component("<module>SomeClass")} whenever two modules end up
     * with the same class name under the {@code com.aiqaos}-wide scan.
     *
     * <p>Four of these were qualified from the start; {@code SlackNotificationSender} and
     * {@code TeamsNotificationSender} were not, and that omission is precisely what made the
     * dashboard unbootable once FI-PE3-D put both modules on its classpath (ADR-089).
     */
    private static final Set<String> KNOWN_QUALIFIED_COLLISIONS = Set.of(
            "SlackNotificationSender",
            "TeamsNotificationSender",
            "WorkflowMetricsCollector",
            "ExecutionMetricsCollector",
            "DashboardService",
            "SecurityMetricsCollector");

    /**
     * Early warning for the ADR-089 failure mode. Spring fails the context on a duplicate bean
     * <i>name</i>, and {@link #theApplicationContextStarts()} already covers that — but it only
     * surfaces the first clash it hits, and it says nothing about a new same-named pair that happens
     * to be qualified today and could be copied unqualified tomorrow.
     *
     * <p>So this reports every colliding simple name at once and fails on any that is not in the
     * known-and-qualified inventory, forcing a deliberate decision rather than a silent addition.
     */
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
                "a new cross-module class-name collision appeared. Under this application's "
                        + "com.aiqaos-wide component scan, two beans with the same simple name both "
                        + "claim the same default bean name and the context fails to start. Give each "
                        + "an explicit qualified name — @Component(\"<module>SomeClass\"), as the "
                        + "existing collisions do — then add it to KNOWN_QUALIFIED_COLLISIONS: "
                        + unexpected);
    }
}
