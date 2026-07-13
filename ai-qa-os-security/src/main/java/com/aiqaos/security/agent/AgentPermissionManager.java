package com.aiqaos.security.agent;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

@Component
public class AgentPermissionManager {

    private static final Map<String, Set<String>> ALLOWED = Map.of(
        "QA_ANALYST",            Set.of("READ_REQUIREMENT", "ANALYZE_RISK", "GENERATE_TEST_CASE"),
        "AUTOMATION_GENERATOR",  Set.of("READ_TEST_CASES", "WRITE_CODE", "WRITE_SCRIPTS"),
        "BUG_ANALYZER",          Set.of("READ_LOGS", "READ_STACK_TRACES", "CREATE_BUG_REPORT"),
        "API_TESTER",            Set.of("READ_API_SPECS", "EXECUTE_API"),
        "REPORT_ANALYST",        Set.of("READ_EXECUTION_RESULTS", "GENERATE_DASHBOARDS")
    );

    private static final Set<String> GLOBAL_BLOCKED = Set.of(
        "DELETE_FILE", "DROP_DATABASE", "EXECUTE_COMMAND", "DELETE_RECORD"
    );

    public boolean isAllowed(String agentType, String action) {
        if (GLOBAL_BLOCKED.contains(action)) return false;
        Set<String> allowed = ALLOWED.getOrDefault(agentType, Set.of());
        return allowed.contains(action);
    }
}