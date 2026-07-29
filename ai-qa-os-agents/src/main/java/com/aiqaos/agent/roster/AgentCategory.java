package com.aiqaos.agent.roster;

/**
 * AGT-1: the functional category an agent belongs to in the roster, matching the roadmap's roster
 * table. Non-functional (performance/security/accessibility/visual) and platform-engineering
 * categories are where most Future specialist agents land.
 */
public enum AgentCategory {

    REQUIREMENT_INTELLIGENCE("Requirement Intelligence"),
    TEST_DESIGN("Test Design"),
    AUTOMATION("Automation"),
    EXECUTION("Execution"),
    EXECUTION_INTELLIGENCE("Execution Intelligence"),
    REPORTING("Reporting"),
    NON_FUNCTIONAL("Non-functional"),
    PLATFORM_ENGINEERING("Platform Engineering");

    private final String displayName;

    AgentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
