package com.aiqaos.core.requirement;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class RequirementParserTest {

    private final RequirementParser parser = new RequirementParser();
    private final RequirementReader reader = new RequirementReader();

    @Test
    public void testParseUS001Success() throws IOException {
        String filePath = "D:\\QA AI Automation\\AI-QA-OS Architecture\\AI-QA-OS-Core\\resources\\user-stories\\Login\\US-001.md";
        String rawContent = reader.readRequirement(filePath);
        RequirementContext context = parser.parse(rawContent);

        assertNotNull(context);
        assertEquals("Admin Login", context.getTitle());
        assertTrue(context.getDescription().contains("As an admin, I want to log in"));
        
        assertNotNull(context.getAcceptanceCriteria());
        assertTrue(context.getAcceptanceCriteria().size() > 0);
        
        System.out.println("=== Requirement Context Verification ===");
        System.out.println("Feature Title: " + context.getTitle());
        System.out.println("Description: " + context.getDescription());
        System.out.println("Acceptance Criteria List:");
        context.getAcceptanceCriteria().forEach(c -> System.out.println("  - " + c));
        System.out.println("Raw Content:\n" + context.getRawContent());
        System.out.println("=========================================");

        // Assert first few criteria elements exist
        assertTrue(context.getAcceptanceCriteria().stream()
            .anyMatch(c -> c.contains("The login page should have fields for Email/Username and Password.")));
        assertTrue(context.getAcceptanceCriteria().stream()
            .anyMatch(c -> c.contains("The Username must be the admin’s registered email address only.")));
    }
}
