package com.aiqaos.gateway.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentationGeneratorTest {

    @Test
    @DisplayName("DX-7: Should generate all 4 developer documentation artifacts")
    void testGenerateAllDocs(@TempDir Path tempDir) throws IOException {
        DocumentationGenerator generator = new DocumentationGenerator();
        generator.generateAllDocs(tempDir.toString());

        File apiDocs = tempDir.resolve("API-Documentation.md").toFile();
        File agentDocs = tempDir.resolve("Agent-Catalogue.md").toFile();
        File archDocs = tempDir.resolve("Architecture-Overview.md").toFile();
        File cliDocs = tempDir.resolve("CLI-Manual.md").toFile();

        assertTrue(apiDocs.exists(), "API-Documentation.md should be created");
        assertTrue(agentDocs.exists(), "Agent-Catalogue.md should be created");
        assertTrue(archDocs.exists(), "Architecture-Overview.md should be created");
        assertTrue(cliDocs.exists(), "CLI-Manual.md should be created");

        String apiContent = Files.readString(apiDocs.toPath());
        assertTrue(apiContent.contains("POST /brain/analyze"), "API docs should include endpoint");

        String agentContent = Files.readString(agentDocs.toPath());
        assertTrue(agentContent.contains("StepRequirementReader"), "Agent docs should include agents");

        String archContent = Files.readString(archDocs.toPath());
        assertTrue(archContent.contains("Architectural Invariants"), "Architecture docs should include rules");

        String cliContent = Files.readString(cliDocs.toPath());
        assertTrue(cliContent.contains("qaos doctor"), "CLI manual should include commands");
    }
}
