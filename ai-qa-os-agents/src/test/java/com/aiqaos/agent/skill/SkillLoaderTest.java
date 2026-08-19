package com.aiqaos.agent.skill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SkillLoaderTest {

    private SkillLoader skillLoader;

    @BeforeEach
    public void setUp() {
        skillLoader = new SkillLoader();
    }

    @Test
    public void testLoadExistingSkill() {
        Optional<String> instructions = skillLoader.loadSkillInstructions("automation-generation");
        assertTrue(instructions.isPresent(), "Expected automation-generation skill resource to be loaded");
        String content = instructions.get();
        assertFalse(content.contains("---"), "Frontmatter delimiters should be stripped");
        assertTrue(content.contains("Automation Script Generation Skill") || content.contains("Playwright"),
                "Skill content should contain instructions body");
    }

    @Test
    public void testLoadNonExistentSkillReturnsEmpty() {
        Optional<String> instructions = skillLoader.loadSkillInstructions("non-existent-skill-name-12345");
        assertTrue(instructions.isEmpty(), "Non-existent skill should return Optional.empty()");
    }

    @Test
    public void testLoadNullOrEmptySkillNameReturnsEmpty() {
        assertTrue(skillLoader.loadSkillInstructions(null).isEmpty());
        assertTrue(skillLoader.loadSkillInstructions("   ").isEmpty());
    }
}
