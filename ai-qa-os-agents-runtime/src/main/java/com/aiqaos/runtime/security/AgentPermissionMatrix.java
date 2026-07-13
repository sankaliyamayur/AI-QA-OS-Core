package com.aiqaos.runtime.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AgentPermissionMatrix {

    private static final Map<String, Set<String>> permissions = new HashMap<>();

    static {
        permissions.put("QA_ANALYST", Set.of("READ_REQUIREMENTS", "READ_CODE", "ANALYZE_RISKS"));
        permissions.put("TEST_DESIGNER", Set.of("READ_REQUIREMENTS", "WRITE_TEST_CASES"));
        permissions.put("AUTOMATION_GENERATOR", Set.of("READ_TEST_CASES", "WRITE_CODE", "WRITE_SCRIPTS"));
        permissions.put("API_TESTER", Set.of("READ_API_SPECS", "EXECUTE_API"));
        permissions.put("MOBILE_TESTER", Set.of("READ_SPECS", "EXECUTE_MOBILE"));
        permissions.put("BUG_ANALYZER", Set.of("READ_LOGS", "READ_STACK_TRACES", "CREATE_BUG_REPORT"));
        permissions.put("REPORT_ANALYST", Set.of("READ_EXECUTION_RESULTS", "GENERATE_DASHBOARDS"));
    }

    public static Set<String> getPermissionsForAgent(String agentType) {
        return permissions.getOrDefault(agentType, Collections.emptySet());
    }

    public static boolean hasPermission(String agentType, String action) {
        return getPermissionsForAgent(agentType).contains(action);
    }
}
