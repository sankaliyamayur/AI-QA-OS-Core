package com.aiqaos.core.skill;

import java.util.Optional;

/**
 * Contract for loading AI agent skill instructions.
 * Implementation lives in ai-qa-os-agents to preserve classpath ownership.
 */
public interface SkillContract {
    /**
     * Loads the instruction body of the specified skill name.
     *
     * @param skillName the name of the skill (e.g. "automation-generation")
     * @return Optional containing the instruction body, or Optional.empty() if missing
     */
    Optional<String> loadSkillInstructions(String skillName);
}
