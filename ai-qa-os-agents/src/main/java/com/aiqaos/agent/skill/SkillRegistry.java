package com.aiqaos.agent.skill;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry mapping AgentType enum values to their corresponding AI-QA-OS runtime skill resource names.
 */
@Component
public class SkillRegistry {

    private final Map<AgentType, String> registry;

    public SkillRegistry() {
        Map<AgentType, String> map = new EnumMap<>(AgentType.class);
        map.put(AgentType.PLANNING, "user-story-analysis");
        map.put(AgentType.QA_ENGINEER, "qa-analysis");
        map.put(AgentType.TEST_CASE_GENERATOR, "test-case-generation");
        map.put(AgentType.SCRIPT_GENERATOR, "automation-generation");
        map.put(AgentType.EXECUTION_ENGINEER, "playwright-execution");
        map.put(AgentType.BUG_ANALYZER, "bug-analysis");
        map.put(AgentType.SELF_HEALING_ENGINEER, "self-healing");
        map.put(AgentType.LEARNING_ENGINEER, "learning-analysis");
        map.put(AgentType.REPORTER, "qa-reporting");
        this.registry = Collections.unmodifiableMap(map);
    }

    /**
     * Resolves the skill resource name for a given AgentType.
     *
     * @param agentType the AgentType enum
     * @return Optional containing skill name, or Optional.empty() if unmapped
     */
    public Optional<String> resolveSkillName(AgentType agentType) {
        if (agentType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registry.get(agentType));
    }
}
