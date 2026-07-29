package com.aiqaos.agent.roster;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * AGT-1: the platform's agent roster as a first-class, queryable catalog. Seeded from the roadmap's
 * roster table (18 agents — 6 implemented, 2 designed, 10 future), it makes coverage and gaps
 * programmatic. The Future specialist agents are built incrementally via DX-2/PLG-3 (FI-AGT1-A); as
 * each lands, its descriptor flips to {@code IMPLEMENTED}.
 */
@Component
public class AgentRoster {

    private final List<AgentDescriptor> roster = new ArrayList<>();

    public AgentRoster() {
        // Implemented (6)
        add("Requirement Agent", AgentCategory.REQUIREMENT_INTELLIGENCE, AgentStatus.IMPLEMENTED, "QAAnalystAgent");
        add("Test Case Agent", AgentCategory.TEST_DESIGN, AgentStatus.IMPLEMENTED, "TestCaseGeneratorAgent");
        add("Automation Agent", AgentCategory.AUTOMATION, AgentStatus.IMPLEMENTED, "ScriptGeneratorAgent");
        add("Execution Agent", AgentCategory.EXECUTION, AgentStatus.IMPLEMENTED, "ExecutionEngineerAgent");
        add("Self Healing Agent", AgentCategory.EXECUTION_INTELLIGENCE, AgentStatus.IMPLEMENTED, "SelfHealingAgent");
        add("Reporting Agent", AgentCategory.REPORTING, AgentStatus.IMPLEMENTED, "ReportingAgent");
        // Designed (2)
        add("Scenario Agent", AgentCategory.TEST_DESIGN, AgentStatus.DESIGNED, null);
        add("Test Data Agent", AgentCategory.TEST_DESIGN, AgentStatus.DESIGNED, null);
        // Future specialist agents (10)
        add("API Agent", AgentCategory.AUTOMATION, AgentStatus.FUTURE, null);
        add("Mobile Agent", AgentCategory.AUTOMATION, AgentStatus.FUTURE, null);
        add("Performance Agent", AgentCategory.NON_FUNCTIONAL, AgentStatus.FUTURE, null);
        add("Security Agent", AgentCategory.NON_FUNCTIONAL, AgentStatus.FUTURE, null);
        add("Accessibility Agent", AgentCategory.NON_FUNCTIONAL, AgentStatus.FUTURE, null);
        add("Visual Testing Agent", AgentCategory.NON_FUNCTIONAL, AgentStatus.FUTURE, null);
        add("Database Agent", AgentCategory.AUTOMATION, AgentStatus.FUTURE, null);
        add("Architecture Agent", AgentCategory.PLATFORM_ENGINEERING, AgentStatus.FUTURE, null);
        add("Code Review Agent", AgentCategory.PLATFORM_ENGINEERING, AgentStatus.FUTURE, null);
        add("Release Agent", AgentCategory.PLATFORM_ENGINEERING, AgentStatus.FUTURE, null);
    }

    private void add(String name, AgentCategory category, AgentStatus status, String impl) {
        roster.add(new AgentDescriptor(name, category, status, impl));
    }

    public List<AgentDescriptor> all() {
        return new ArrayList<>(roster);
    }

    public List<AgentDescriptor> byCategory(AgentCategory category) {
        return roster.stream().filter(d -> d.getCategory() == category).toList();
    }

    public List<AgentDescriptor> byStatus(AgentStatus status) {
        return roster.stream().filter(d -> d.getStatus() == status).toList();
    }

    public Optional<AgentDescriptor> find(String name) {
        return roster.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst();
    }

    /** Categories that have at least one rostered agent. */
    public Set<AgentCategory> categories() {
        Set<AgentCategory> set = EnumSet.noneOf(AgentCategory.class);
        roster.forEach(d -> set.add(d.getCategory()));
        return set;
    }

    public int total() {
        return roster.size();
    }

    public long implementedCount() {
        return roster.stream().filter(AgentDescriptor::isImplemented).count();
    }

    /** Fraction of the roster that is implemented (0..1). */
    public double coverageRatio() {
        return roster.isEmpty() ? 0.0 : (double) implementedCount() / roster.size();
    }
}
