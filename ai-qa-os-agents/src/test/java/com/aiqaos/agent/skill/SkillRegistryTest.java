package com.aiqaos.agent.skill;

import com.aiqaos.core.enums.AgentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SkillRegistryTest {

    private SkillRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new SkillRegistry();
    }

    @Test
    public void testAllActiveAgentTypesResolveCorrectly() {
        assertEquals("qa-analysis", registry.resolveSkillName(AgentType.QA_ENGINEER).orElse(null));
        assertEquals("test-case-generation", registry.resolveSkillName(AgentType.TEST_CASE_GENERATOR).orElse(null));
        assertEquals("automation-generation", registry.resolveSkillName(AgentType.SCRIPT_GENERATOR).orElse(null));
        assertEquals("playwright-execution", registry.resolveSkillName(AgentType.EXECUTION_ENGINEER).orElse(null));
        assertEquals("bug-analysis", registry.resolveSkillName(AgentType.BUG_ANALYZER).orElse(null));
        assertEquals("self-healing", registry.resolveSkillName(AgentType.SELF_HEALING_ENGINEER).orElse(null));
        assertEquals("learning-analysis", registry.resolveSkillName(AgentType.LEARNING_ENGINEER).orElse(null));
        assertEquals("qa-reporting", registry.resolveSkillName(AgentType.REPORTER).orElse(null));
    }

    @Test
    public void testUnmappedAgentTypesReturnEmpty() {
        assertTrue(registry.resolveSkillName(AgentType.VALIDATOR).isEmpty());
        assertTrue(registry.resolveSkillName(AgentType.CODER).isEmpty());
        assertTrue(registry.resolveSkillName(AgentType.SUPERVISOR).isEmpty());
    }

    @Test
    public void testNullAgentTypeReturnsEmpty() {
        assertTrue(registry.resolveSkillName(null).isEmpty());
    }
}
