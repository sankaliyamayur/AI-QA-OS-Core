package com.aiqaos.gateway.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScaffoldingGeneratorTest {

    @Test
    @DisplayName("DX-2: Should scaffold Java AI Agent class")
    void testGenerateAgent(@TempDir Path tempDir) throws IOException {
        ScaffoldingGenerator generator = new ScaffoldingGenerator();
        String filePath = generator.generateAgent("PaymentValidation", tempDir.toString());

        File file = new File(filePath);
        assertTrue(file.exists(), "Agent file should be created");
        String content = Files.readString(file.toPath());
        assertTrue(content.contains("public class PaymentValidationAgent"), "Class declaration should match name");
        assertTrue(content.contains("@Component(\"paymentValidationAgent\")"), "Spring component annotation should be present");
    }

    @Test
    @DisplayName("DX-2: Should scaffold YAML Workflow definition")
    void testGenerateWorkflow(@TempDir Path tempDir) throws IOException {
        ScaffoldingGenerator generator = new ScaffoldingGenerator();
        String filePath = generator.generateWorkflow("CheckoutRegression", tempDir.toString());

        File file = new File(filePath);
        assertTrue(file.exists(), "Workflow YAML should be created");
        String content = Files.readString(file.toPath());
        assertTrue(content.contains("name: \"checkout-regression\""), "YAML name should be hyphenated");
        assertTrue(content.contains("StepRequirementReader"), "YAML should include standard step agents");
    }

    @Test
    @DisplayName("DX-2: Should scaffold Prompt Template file")
    void testGeneratePrompt(@TempDir Path tempDir) throws IOException {
        ScaffoldingGenerator generator = new ScaffoldingGenerator();
        String filePath = generator.generatePrompt("LocatorExtractor", tempDir.toString());

        File file = new File(filePath);
        assertTrue(file.exists(), "Prompt file should be created");
        String content = Files.readString(file.toPath());
        assertTrue(content.contains("[SYSTEM_ROLE]"), "Prompt template should contain system role section");
        assertTrue(content.contains("[USER_TEMPLATE]"), "Prompt template should contain user template section");
    }

    @Test
    @DisplayName("DX-2: Should scaffold Maven Module POM template")
    void testGenerateModule(@TempDir Path tempDir) throws IOException {
        ScaffoldingGenerator generator = new ScaffoldingGenerator();
        String filePath = generator.generateModule("security-audit", tempDir.toString());

        File file = new File(filePath);
        assertTrue(file.exists(), "Module POM should be created");
        String content = Files.readString(file.toPath());
        assertTrue(content.contains("<artifactId>ai-qa-os-security-audit</artifactId>"), "POM artifactId should have prefix");
        assertTrue(content.contains("ai-qa-os-core"), "POM should depend on core");
    }
}
