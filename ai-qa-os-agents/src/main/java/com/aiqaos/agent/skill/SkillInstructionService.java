package com.aiqaos.agent.skill;

import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.skill.SkillContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Centralized service for resolving and composing system instructions for AI-QA-OS agents.
 * Implements three-layer instruction composition:
 *   1. [SKILL INSTRUCTIONS] (from SKILL.md resource)
 *   2. [AGENT INSTRUCTIONS] (agent default persona/role)
 *   3. [REQUEST-SPECIFIC INSTRUCTIONS] (from AgentRequest systemInstruction, if present)
 */
@Service
public class SkillInstructionService {

    private static final Logger log = LoggerFactory.getLogger(SkillInstructionService.class);

    private final SkillRegistry skillRegistry;
    private final SkillContract skillContract;

    @Autowired
    public SkillInstructionService(SkillRegistry skillRegistry, SkillContract skillContract) {
        this.skillRegistry = skillRegistry;
        this.skillContract = skillContract;
    }

    /**
     * Builds the complete composed system prompt for an agent execution.
     *
     * @param agentType          the agent's AgentType enum constant
     * @param agentDefault       the agent's default system prompt string
     * @param requestInstruction optional request-specific instruction string
     * @return composed system prompt string
     */
    public String build(AgentType agentType, String agentDefault, String requestInstruction) {
        Optional<String> skillBody = skillRegistry.resolveSkillName(agentType)
                .flatMap(skillContract::loadSkillInstructions);

        if (skillBody.isPresent()) {
            log.debug("[SkillInstructionService] Applied skill for AgentType: {}", agentType);
            StringBuilder sb = new StringBuilder();
            sb.append("=== SKILL INSTRUCTIONS ===\n");
            sb.append(skillBody.get()).append("\n\n");
            sb.append("=== AGENT INSTRUCTIONS ===\n");
            sb.append(agentDefault != null ? agentDefault : "");

            if (requestInstruction != null && !requestInstruction.trim().isEmpty()) {
                sb.append("\n\n=== REQUEST-SPECIFIC INSTRUCTIONS ===\n");
                sb.append(requestInstruction.trim());
            }
            return sb.toString();
        }

        // Fallback when no skill is mapped or resource is missing:
        if (requestInstruction != null && !requestInstruction.trim().isEmpty()) {
            return (agentDefault != null ? agentDefault + "\n\n" : "") + requestInstruction.trim();
        }
        return agentDefault != null ? agentDefault : "";
    }
}
