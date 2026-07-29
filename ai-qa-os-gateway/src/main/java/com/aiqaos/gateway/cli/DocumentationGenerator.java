package com.aiqaos.gateway.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DX-7: Developer Documentation Generator.
 *
 * Generates technical markdown documentation for the AI-QA-OS platform:
 *   1. API-Documentation.md    — REST API endpoints & schemas
 *   2. Agent-Catalogue.md     — AI Agent capabilities & contracts
 *   3. Architecture-Overview.md— Multi-module dependencies & layer rules
 *   4. CLI-Manual.md           — Developer CLI commands & usage
 */
public class DocumentationGenerator {

    private static final Logger log = LoggerFactory.getLogger(DocumentationGenerator.class);

    public String generateAllDocs(String outputDir) throws IOException {
        Path baseDir = Path.of(outputDir != null ? outputDir : "docs/generated");
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        generateApiDocs(baseDir.toString());
        generateAgentCatalogue(baseDir.toString());
        generateArchitectureOverview(baseDir.toString());
        generateCliManual(baseDir.toString());

        log.info("DX-7: All 4 developer documentation artifacts generated in {}", baseDir.toAbsolutePath());
        return baseDir.toAbsolutePath().toString();
    }

    public String generateApiDocs(String outputDir) throws IOException {
        String md = """
            # AI-QA-OS REST API Reference (DX-7 Generated)

            ## Gateway Endpoints (:8082)

            ### Workflows
            - `POST /brain/analyze` — Run AI requirement analysis on input text
            - `POST /workflow/run` — Trigger autonomous QA pipeline execution
            - `GET /workflow/status/{id}` — Query execution status & step details
            - `GET /workflow/list` — List registered workflows

            ### Human Review
            - `GET /human-review/pending` — List executions requiring human review
            - `POST /human-review/approve` — Approve or reject pending execution step

            ### Actuator & Observability
            - `GET /actuator/health` — Service health check
            - `GET /actuator/prometheus` — Micrometer metrics scrape endpoint

            ## Dashboard API (:8090)
            - `GET /api/executions` — Execution history list
            - `GET /api/executions/{id}` — Detailed execution record
            - `GET /api/test-cases` — Test case catalog & pass rates
            - `GET /api/modules` — Module metrics summary
            """;
        return writeToFile(outputDir, "API-Documentation.md", md);
    }

    public String generateAgentCatalogue(String outputDir) throws IOException {
        String md = """
            # AI-QA-OS Agent Catalogue (DX-7 Generated)

            ## Active Pipeline Step Agents

            | Agent Name | Description | Module | Primary Contract |
            |---|---|---|---|
            | `StepRequirementReader` | Reads US/PRD inputs & extracts raw scenarios | `ai-qa-os-brain` | Requirement analysis |
            | `StepQAAnalysis` | Generates QA plan, risk scores, target locators | `ai-qa-os-intelligence` | Intelligence analysis |
            | `StepTestCaseGeneration` | Generates structured functional test cases | `ai-qa-os-agents` | Test case generation |
            | `StepScriptGeneration` | Emits Playwright automation scripts | `ai-qa-os-agents` | Code generation |
            | `StepExecution` | Runs Playwright script in container sandbox | `ai-qa-os-execution` | Script execution |
            | `StepReporting` | Aggregates metrics & HTML report artifacts | `ai-qa-os-reporting` | Reporting |
            | `SelfHealingEngine` | Autonomous locator repair on Playwright failures | `ai-qa-os-healing` | Self-healing |
            | `SimulatorProvider` | Zero-cost local AI simulator for dev/testing | `ai-qa-os-ai-provider` | Local simulation |
            """;
        return writeToFile(outputDir, "Agent-Catalogue.md", md);
    }

    public String generateArchitectureOverview(String outputDir) throws IOException {
        String md = """
            # AI-QA-OS Architecture & Dependency Rules (DX-7 Generated)

            ## Layer Structure
            - **`core`**: Pure domain entities & models. No outward dependencies.
            - **`security`**: Auth, JWT, RBAC, API keys.
            - **`memory`**: Qdrant vector store & semantic cache.
            - **`intelligence`**: Risk scoring & reasoning engines.
            - **`ai-provider`**: Gemini, OpenAI, Claude, & Local Simulator LLM providers.
            - **`orchestration`**: Pipeline orchestrator & state machine.
            - **`gateway`**: REST API gateway, WebSocket, CLI entry point.
            - **`dashboard`**: Management UI backend & history store.

            ## Architectural Invariants (Enforced via ArchUnit DX-6)
            1. Inward Dependency: `core` must not depend on outer modules.
            2. Constructor Injection: No `@Autowired` field injection permitted.
            3. Web Isolation: Core domain models are free of Spring Web MVC dependencies.
            """;
        return writeToFile(outputDir, "Architecture-Overview.md", md);
    }

    public String generateCliManual(String outputDir) throws IOException {
        String md = """
            # AI-QA-OS CLI Manual (DX-7 Generated)

            ## Overview
            The `qaos` CLI provides developer & DevOps commands for platform management.

            ## Usage
            ```bash
            ./scripts/qaos.ps1 <command> [options]
            ./scripts/qaos.sh <command> [options]
            ```

            ## Available Commands
            - `qaos doctor` — Run JVM, DB, and service health checks
            - `qaos version` — Display platform version info
            - `qaos workflow run [--name <n>]` — Trigger an autonomous QA pipeline run
            - `qaos workflow list [--json]` — List registered workflows
            - `qaos execution status [--id <i>]` — Query status of an execution
            - `qaos agent list [--json]` — List registered AI agents
            - `qaos report show [--id <i>]` — Display execution summary report
            - `qaos generate <type> --name <n>` — Scaffold new agent, workflow, prompt, or module
            - `qaos generate docs [--dir <d>]` — Generate developer markdown documentation
            """;
        return writeToFile(outputDir, "CLI-Manual.md", md);
    }

    private String writeToFile(String outputDir, String fileName, String content) throws IOException {
        Path dirPath = Path.of(outputDir != null ? outputDir : "docs/generated");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        File file = dirPath.resolve(fileName).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        log.info("DX-7: Generated documentation file at: {}", file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
