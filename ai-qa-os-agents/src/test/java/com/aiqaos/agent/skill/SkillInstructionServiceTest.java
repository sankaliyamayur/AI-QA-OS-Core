package com.aiqaos.agent.skill;

import com.aiqaos.core.enums.AgentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SkillInstructionServiceTest {

    private SkillInstructionService service;

    @BeforeEach
    public void setUp() {
        SkillRegistry registry = new SkillRegistry();
        SkillLoader loader = new SkillLoader();
        service = new SkillInstructionService(registry, loader);
    }

    @Test
    public void testThreeLayerCompositionWhenAllPresent() {
        String result = service.build(AgentType.SCRIPT_GENERATOR, "Default Persona", "Custom System Instruction");
        assertTrue(result.contains("=== SKILL INSTRUCTIONS ==="), "Skill header should be present");
        assertTrue(result.contains("=== AGENT INSTRUCTIONS ==="), "Agent header should be present");
        assertTrue(result.contains("Default Persona"), "Default persona should be present");
        assertTrue(result.contains("=== REQUEST-SPECIFIC INSTRUCTIONS ==="), "Request header should be present");
        assertTrue(result.contains("Custom System Instruction"), "Request instruction should be present");

        int skillIdx = result.indexOf("=== SKILL INSTRUCTIONS ===");
        int agentIdx = result.indexOf("=== AGENT INSTRUCTIONS ===");
        int reqIdx = result.indexOf("=== REQUEST-SPECIFIC INSTRUCTIONS ===");
        assertTrue(skillIdx < agentIdx && agentIdx < reqIdx, "Order must be Skill -> Agent -> Request");
    }

    @Test
    public void testCompositionWithoutRequestInstruction() {
        String result = service.build(AgentType.QA_ENGINEER, "QA Persona", null);
        assertTrue(result.contains("=== SKILL INSTRUCTIONS ==="));
        assertTrue(result.contains("=== AGENT INSTRUCTIONS ==="));
        assertFalse(result.contains("=== REQUEST-SPECIFIC INSTRUCTIONS ==="));
    }

    @Test
    public void testFallbackWhenUnmappedAgentType() {
        String result = service.build(AgentType.VALIDATOR, "Validator Persona", "Request Instruction");
        assertFalse(result.contains("=== SKILL INSTRUCTIONS ==="));
        assertEquals("Validator Persona\n\nRequest Instruction", result);
    }
}
