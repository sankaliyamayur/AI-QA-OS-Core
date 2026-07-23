package com.aiqaos.core.requirement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class RequirementReaderTest {

    private final RequirementReader reader = new RequirementReader();

    @Test
    @Disabled("MNT-1 quarantine: pre-existing failure (hardcoded absolute path); re-enable when fixed — see FI-MNT1-A")
    public void testReadRequirementSuccess() throws IOException {
        String filePath = "D:\\QA AI Automation\\AI-QA-OS Architecture\\AI-QA-OS-Core\\resources\\user-stories\\Login\\US-001.md";
        String content = reader.readRequirement(filePath);
        assertNotNull(content);
        assertTrue(content.contains("Title: Admin Login"));
    }

    @Test
    public void testReadRequirementFileNotFound() {
        String filePath = "D:\\invalid_path_to_story.md";
        assertThrows(IOException.class, () -> {
            reader.readRequirement(filePath);
        });
    }
}
