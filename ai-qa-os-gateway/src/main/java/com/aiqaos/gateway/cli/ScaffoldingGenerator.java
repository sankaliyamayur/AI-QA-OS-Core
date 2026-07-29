package com.aiqaos.gateway.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DX-2: Scaffolding Generators.
 *
 * Generates boilerplates and templates for:
 *   - AI Agents (Java component extending agent pipeline contract)
 *   - Workflows (YAML workflow definition)
 *   - Prompts (Structured system & user prompt templates)
 *   - Modules (Maven POM template for new platform modules)
 */
public class ScaffoldingGenerator {

    private static final Logger log = LoggerFactory.getLogger(ScaffoldingGenerator.class);

    /**
     * Generates a Java AI Agent template.
     */
    public String generateAgent(String agentName, String outputDir) throws IOException {
        String className = capitalize(agentName);
        if (!className.endsWith("Agent")) {
            className += "Agent";
        }

        String code = String.format("""
            package com.aiqaos.agents;

            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.stereotype.Component;

            /**
             * Auto-generated AI-QA-OS Agent: %s
             * Scaffolding generated via 'qaos generate agent' (DX-2).
             */
            @Component("%s")
            public class %s {

                private static final Logger log = LoggerFactory.getLogger(%s.class);

                public %s() {
                    log.info("%s initialized into agent registry");
                }

                public String execute(String input) {
                    log.info("Executing %s with input length: {}", input != null ? input.length() : 0);
                    // TODO: Implement AI agent reasoning logic here
                    return "Result from %s";
                }
            }
            """, className, decapitalize(className), className, className, className, className, className, className);

        return writeToFile(outputDir, className + ".java", code);
    }

    /**
     * Generates a YAML Workflow definition template.
     */
    public String generateWorkflow(String workflowName, String outputDir) throws IOException {
        String name = toKebabCase(workflowName);
        String yaml = String.format("""
            # Auto-generated AI-QA-OS Workflow Definition: %s
            # Scaffolding generated via 'qaos generate workflow' (DX-2).
            version: "1.0"
            name: "%s"
            description: "Custom QA workflow definition for %s"
            environment: "staging"
            timeoutSeconds: 300

            steps:
              - id: "step-1"
                name: "Requirement Analysis"
                agent: "StepRequirementReader"
                onFailure: "ABORT"

              - id: "step-2"
                name: "QA Metric Generation"
                agent: "StepQAAnalysis"
                onFailure: "PAUSE_FOR_HUMAN_REVIEW"

              - id: "step-3"
                name: "Test Case Generation"
                agent: "StepTestCaseGeneration"
                onFailure: "ABORT"

              - id: "step-4"
                name: "Playwright Execution"
                agent: "StepExecution"
                onFailure: "TRIGGER_SELF_HEALING"

            qualityGate:
              minPassRatePercent: 90
              requireHumanApprovalOnFailure: true
            """, name, name, name);

        return writeToFile(outputDir, name + "-workflow.yaml", yaml);
    }

    /**
     * Generates a Prompt template file.
     */
    public String generatePrompt(String promptName, String outputDir) throws IOException {
        String name = toKebabCase(promptName);
        String promptContent = String.format("""
            # Auto-generated AI-QA-OS Prompt Template: %s
            # Scaffolding generated via 'qaos generate prompt' (DX-2).
            
            [SYSTEM_ROLE]
            You are an expert Quality Engineering AI Assistant specialized in test case design,
            locator extraction, and Playwright test script generation.

            [CONTEXT]
            Domain: Quality Engineering & Autonomous Testing
            Version: 1.0.0

            [INSTRUCTIONS]
            Given the input requirement specification or user story:
            1. Analyze functional requirements and edge cases.
            2. Extract key UI interactors and locators.
            3. Output test assertions in strict JSON format.

            [USER_TEMPLATE]
            Requirement Source: {{REQUIREMENT_TEXT}}
            Target Environment: {{ENVIRONMENT}}

            [OUTPUT_FORMAT]
            {
              "scenarios": [],
              "locators": [],
              "assertions": []
            }
            """, name);

        return writeToFile(outputDir, name + ".prompt", promptContent);
    }

    /**
     * Generates a Maven POM template for a new child module.
     */
    public String generateModule(String moduleName, String outputDir) throws IOException {
        String name = toKebabCase(moduleName);
        String artifactId = name.startsWith("ai-qa-os-") ? name : "ai-qa-os-" + name;
        String pomXml = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Auto-generated AI-QA-OS Module POM: %s -->
            <!-- Scaffolding generated via 'qaos generate module' (DX-2). -->
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <parent>
                    <groupId>com.aiqaos</groupId>
                    <artifactId>ai-qa-os</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                </parent>

                <artifactId>%s</artifactId>
                <packaging>jar</packaging>

                <name>%s</name>
                <description>AI-QA-OS Module: %s</description>

                <dependencies>
                    <dependency>
                        <groupId>com.aiqaos</groupId>
                        <artifactId>ai-qa-os-core</artifactId>
                        <version>${project.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """, artifactId, artifactId, artifactId, artifactId);

        return writeToFile(outputDir, artifactId + "-pom.xml", pomXml);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String writeToFile(String outputDir, String fileName, String content) throws IOException {
        Path dirPath = Path.of(outputDir != null ? outputDir : ".");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        File file = dirPath.resolve(fileName).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        log.info("DX-2: Scaffolding generated file at: {}", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    private String toKebabCase(String input) {
        if (input == null || input.isEmpty()) return "custom";
        return input.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("[\\s_]+", "-")
                .toLowerCase();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Custom";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String decapitalize(String str) {
        if (str == null || str.isEmpty()) return "custom";
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
