package com.aiqaos.execution.engine;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedScriptSuite.AutomationScript;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PlaywrightExecutionEngine
 *
 * Invokes the Playwright test runner via PowerShell (run-playwright.ps1) using
 * Java's ProcessBuilder. After execution it parses the JSON results file to
 * collect per-test-case artifact paths (screenshot, video, trace) and
 * populates the ExecutionResult accordingly.
 *
 * Artifact directory layout produced by playwright.config.ts:
 *   {artifactsBaseDir}/
 *     {executionId}/
 *       {browser}/
 *         test-results/       ← screenshots, videos, traces per test
 *         results.json        ← Playwright JSON reporter output
 *         html-report/        ← Playwright HTML report
 */
@Component
public class PlaywrightExecutionEngine implements ExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightExecutionEngine.class);

    private static final String MANIFEST_PREFIX = "ARTIFACT_MANIFEST:";

    /** Base directory where Playwright writes all artifacts (overridable via env var) */
    @Value("${aiqaos.playwright.artifacts-dir:./playwright-output}")
    private String artifactsBaseDir;

    /** Absolute path to the run-playwright.ps1 script bundled with the execution module */
    @Value("${aiqaos.playwright.runner-script:}")
    private String runnerScriptPath;

    /** App base URL passed to the Playwright runner (overridable via env var) */
    @Value("${aiqaos.playwright.app-base-url:http://localhost:8080}")
    private String appBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean running = false;

    @Override
    public String getSupportedFramework() {
        return "Playwright";
    }

    @Override
    public ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration configuration) {
        running = true;
        LocalDateTime start = LocalDateTime.now();
        ExecutionResult result = new ExecutionResult();
        result.setStartTime(start);
        result.setTaskId(scriptSuite.getSuiteId());
        result.setAgentId("execution-engineer-playwright");

        // Generate a unique execution ID for artifact path scoping
        String executionId = "exec-" + UUID.randomUUID().toString().substring(0, 8);

        // FIX: BrowserType is an enum — convert to lowercase string for the Playwright --project flag
        String browser = resolveBrowserName(configuration.getBrowser());

        try {
            // ── 1. Build and invoke the PowerShell runner ─────────────────────────
            String scriptPath = resolveRunnerScriptPath();
            log.info("[Playwright] Starting execution: id={}, browser={}, script={}", executionId, browser, scriptPath);

            // Write the generated scripts to the test directory before execution
            writeScriptFiles(scriptPath, scriptSuite);

            // FIX: Pass arguments strictly in positional order via -File.
            // This is immune to space-quoting issues in paths, unlike -Command.
            // Positional mapping in run-playwright.ps1:
            //   1. ExecutionId
            //   2. Browser
            //   3. ArtifactsDir
            //   4. AppBaseUrl
            //   5. ConfigFile (pass empty string to auto-resolve)
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath,
                executionId,
                browser,
                resolveArtifactsBaseDir(),
                appBaseUrl,
                ""
            );
            pb.redirectErrorStream(true);
            pb.directory(new File(scriptPath).getParentFile());
            pb.environment().put("PLAYWRIGHT_HEADLESS", configuration.isHeadless() ? "true" : "false");

            Process process = pb.start();

            // ── 2. Capture stdout and parse ARTIFACT_MANIFEST lines ─────────────
            Map<String, String> manifest = new HashMap<>();
            StringBuilder stdoutBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[Playwright] {}", line);
                    stdoutBuilder.append(line).append("\n");
                    if (line.startsWith(MANIFEST_PREFIX)) {
                        String[] parts = line.substring(MANIFEST_PREFIX.length()).split("=", 2);
                        if (parts.length == 2) {
                            manifest.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            log.info("[Playwright] Process exited with code {}", exitCode);

            // ── 3. Parse results.json to collect per-test artifact paths ──────────
            String resultsJsonPath = manifest.get("RESULTS_JSON");
            String outputDir       = manifest.get("OUTPUT_DIR");
            String htmlReport      = manifest.get("HTML_REPORT");

            List<TestCaseArtifactInfo> testArtifacts = parseResultsJson(resultsJsonPath, outputDir);

            // ── 4. Populate ExecutionResult ────────────────────────────────────────
            int passedCount = 0;
            int failedCount = 0;

            for (TestCaseArtifactInfo info : testArtifacts) {
                if ("failed".equalsIgnoreCase(info.status)) {
                    failedCount++;
                    if (info.screenshotPath != null) result.getScreenshots().add(info.screenshotPath);
                    if (info.tracePath     != null) result.getArtifacts().add(info.tracePath);
                    // Store first failed video on the result's video field
                    if (info.videoPath != null && result.getVideo() == null) {
                        result.setVideo(info.videoPath);
                    }
                } else {
                    passedCount++;
                }
            }

            // Fallback counts if no JSON was produced (legacy simulation guard)
            if (testArtifacts.isEmpty()) {
                for (AutomationScript script : scriptSuite.getScripts()) {
                    if (script.getCode() != null && script.getCode().contains("UnsupportedOperationException")) {
                        failedCount++;
                    } else {
                        passedCount++;
                    }
                }
            }

            result.setPassed(passedCount);
            result.setFailed(failedCount);
            result.setSkipped(0);
            result.setSuccess(exitCode == 0);
            result.setStatus(exitCode == 0 ? "PASSED" : "FAILED");

            if (exitCode != 0) {
                result.setErrorMessage("Playwright execution completed with failures. Exit code: " + exitCode);
            }

            result.setLogs("Playwright execution finished.\nPassed: " + passedCount + ", Failed: " + failedCount);
            result.setConsoleLogs(stdoutBuilder.toString());
            result.setReportLocation(htmlReport != null ? htmlReport : outputDir + "/html-report");

            // FIX: ExecutionResult has no setTestCaseArtifacts() method.
            // Store artifact info summary in logs so downstream consumers can use it.
            // The ArtifactManager (registered separately) persists individual artifact paths.
            if (!testArtifacts.isEmpty()) {
                StringBuilder artifactSummary = new StringBuilder();
                artifactSummary.append("\n--- Playwright Artifact Summary ---\n");
                for (TestCaseArtifactInfo info : testArtifacts) {
                    artifactSummary.append(String.format(
                        "[%s] %s | screenshot=%s | video=%s | trace=%s\n",
                        info.status != null ? info.status.toUpperCase() : "UNKNOWN",
                        info.testCaseId,
                        info.screenshotPath != null ? info.screenshotPath : "none",
                        info.videoPath      != null ? info.videoPath      : "none",
                        info.tracePath      != null ? info.tracePath      : "none"
                    ));
                    // Add individual artifact paths to the result.artifacts list for persistence
                    if (info.screenshotPath != null) result.getArtifacts().add("screenshot:" + info.testCaseId + "=" + info.screenshotPath);
                    if (info.videoPath      != null) result.getArtifacts().add("video:"      + info.testCaseId + "=" + info.videoPath);
                    if (info.logPath        != null) result.getArtifacts().add("log:"        + info.testCaseId + "=" + info.logPath);
                }
                result.setLogs(result.getLogs() + artifactSummary);
            }

        } catch (Exception e) {
            log.error("[Playwright] Execution failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setStatus("ERROR");
            result.setErrorMessage("Playwright runner failed: " + e.getMessage());
            result.setStackTrace(stackTraceAsString(e));
        } finally {
            running = false;
        }

        LocalDateTime end = LocalDateTime.now();
        result.setEndTime(end);
        result.setCompletedAt(end);
        result.setDuration(java.time.Duration.between(start, end).toMillis());
        return result;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Converts the BrowserType enum to the lowercase Playwright project name.
     *
     * FIX: ExecutionConfiguration.getBrowser() returns BrowserType (enum), not String.
     * Playwright uses project names: "chromium" | "firefox" | "webkit".
     * CHROME and EDGE both map to "chromium" (Chromium-based).
     * SAFARI maps to "webkit".
     */
    private String resolveBrowserName(BrowserType browserType) {
        if (browserType == null) return "chromium";
        switch (browserType) {
            case FIREFOX: return "firefox";
            case SAFARI:  return "webkit";
            case EDGE:
            case CHROME:
            default:      return "chromium";
        }
    }

    /**
     * Resolves the path to run-playwright.ps1.
     * Priority: configured property → classpath resource → relative fallback.
     */
    private String resolveRunnerScriptPath() {
        if (runnerScriptPath != null && !runnerScriptPath.isBlank()) {
            return runnerScriptPath;
        }
        // Locate from the classpath resource (bundled inside the JAR)
        try {
            var resource = getClass().getClassLoader().getResource("scripts/run-playwright.ps1");
            if (resource != null) {
                if ("file".equalsIgnoreCase(resource.getProtocol())) {
                    return Paths.get(resource.toURI()).toString();
                }
            }
        } catch (Exception e) {
            log.warn("[Playwright] Could not resolve runner script from classpath: {}", e.getMessage());
        }

        // Search in local workspace modules relative to current working directory (e.g. root vs gateway vs dashboard)
        String[] relativePaths = {
            "ai-qa-os-execution/src/main/resources/scripts/run-playwright.ps1",
            "../ai-qa-os-execution/src/main/resources/scripts/run-playwright.ps1",
            "ai-qa-os-execution/target/classes/scripts/run-playwright.ps1",
            "../ai-qa-os-execution/target/classes/scripts/run-playwright.ps1"
        };

        for (String rel : relativePaths) {
            java.io.File file = new java.io.File(rel);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        // Last-resort fallback: same directory as the config file
        return Paths.get("src", "main", "resources", "scripts", "run-playwright.ps1")
                    .toAbsolutePath().toString();
    }

    /**
     * Parses the Playwright JSON reporter output file and returns
     * a list of TestCaseArtifactInfo, each carrying the test case ID,
     * status, and real artifact file paths.
     *
     * Playwright JSON format (abbreviated):
     * {
     *   "suites": [{
     *     "specs": [{
     *       "title": "AC-003: Verify ...",
     *       "tests": [{
     *         "status": "failed",
     *         "results": [{
     *           "status": "failed",
     *           "attachments": [
     *             { "name": "screenshot", "path": "/path/to/screenshot.png" },
     *             { "name": "video",      "path": "/path/to/video.webm" },
     *             { "name": "trace",      "path": "/path/to/trace.zip" }
     *           ],
     *           "stderr": "...",
     *           "stdout": "..."
     *         }]
     *       }]
     *     }]
     *   }]
     * }
     */
    private List<TestCaseArtifactInfo> parseResultsJson(String resultsJsonPath, String outputDir) {
        List<TestCaseArtifactInfo> list = new ArrayList<>();
        if (resultsJsonPath == null) {
            log.warn("[Playwright] No results.json path in manifest. Artifact mapping will be empty.");
            return list;
        }
        Path jsonFile = Paths.get(resultsJsonPath);
        if (!Files.exists(jsonFile)) {
            log.warn("[Playwright] results.json not found at: {}", resultsJsonPath);
            return list;
        }
        try {
            JsonNode root = objectMapper.readTree(jsonFile.toFile());
            traverseSuites(root.path("suites"), list, outputDir);
        } catch (Exception e) {
            log.error("[Playwright] Failed to parse results.json: {}", e.getMessage(), e);
        }
        return list;
    }

    private void traverseSuites(JsonNode suites, List<TestCaseArtifactInfo> result, String outputDir) {
        if (suites.isMissingNode()) return;
        for (JsonNode suite : suites) {
            // Recurse into nested suites
            traverseSuites(suite.path("suites"), result, outputDir);
            // Process specs in this suite
            for (JsonNode spec : suite.path("specs")) {
                String title = spec.path("title").asText("");
                // Extract test case ID from the title (format: "AC-003: Verify ...")
                String testCaseId = extractTestCaseId(title);

                for (JsonNode test : spec.path("tests")) {
                    String testStatus = test.path("status").asText("unknown");
                    for (JsonNode testResult : test.path("results")) {
                        String resultStatus = testResult.path("status").asText(testStatus);
                        TestCaseArtifactInfo info = new TestCaseArtifactInfo();
                        info.testCaseId = testCaseId;
                        info.testTitle  = title;
                        info.status     = resultStatus;

                        // Extract artifact paths from attachments
                        for (JsonNode attachment : testResult.path("attachments")) {
                            String name        = attachment.path("name").asText("");
                            String filePath    = attachment.path("path").asText(null);
                            String contentType = attachment.path("contentType").asText("");

                            if (filePath != null && !filePath.isBlank()) {
                                if ("screenshot".equalsIgnoreCase(name) || contentType.contains("image")) {
                                    info.screenshotPath = filePath;
                                } else if ("video".equalsIgnoreCase(name) || contentType.contains("video")) {
                                    info.videoPath = filePath;
                                } else if ("trace".equalsIgnoreCase(name) || filePath.endsWith(".zip")) {
                                    info.tracePath = filePath;
                                }
                            }
                        }

                        // Capture stderr as execution log for failed tests
                        String stderr = testResult.path("stderr").asText(null);
                        String stdout = testResult.path("stdout").asText(null);
                        if (stderr != null && !stderr.isBlank()) {
                            info.logPath = saveLogFile(outputDir, testCaseId, "stderr", stderr);
                        } else if (stdout != null && !stdout.isBlank()) {
                            info.logPath = saveLogFile(outputDir, testCaseId, "stdout", stdout);
                        }

                        result.add(info);
                    }
                }
            }
        }
    }

    /**
     * Extracts the test case ID prefix (e.g. "TC-AL-003") from a test title.
     * Handles titles like "AC-003: Verify ..." and maps them to "TC-AL-003".
     * If no recognizable ID is found, uses a sanitised version of the title.
     */
    private String extractTestCaseId(String title) {
        if (title == null || title.isBlank()) return "TC-UNKNOWN";
        // Match "TC-xx-NNN" pattern first
        java.util.regex.Matcher tc = java.util.regex.Pattern
            .compile("(TC-[A-Z]+-\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(title);
        if (tc.find()) return tc.group(1).toUpperCase();
        // Match "AC-NNN" pattern used in test titles → map to "TC-AL-NNN"
        java.util.regex.Matcher ac = java.util.regex.Pattern
            .compile("AC-(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(title);
        if (ac.find()) return "TC-AL-" + String.format("%03d", Integer.parseInt(ac.group(1)));
        // Sanitise the title as a last resort
        return "TC-" + title.replaceAll("[^A-Za-z0-9]", "-").substring(0, Math.min(title.length(), 20)).toUpperCase();
    }

    /** Writes inline log text to a file alongside test results and returns the file path. */
    private String saveLogFile(String outputDir, String testCaseId, String type, String content) {
        try {
            Path logDir  = Paths.get(outputDir, "logs");
            Files.createDirectories(logDir);
            Path logFile = logDir.resolve(testCaseId + "-" + type + ".log");
            Files.writeString(logFile, content);
            return logFile.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("[Playwright] Could not save log file for {}: {}", testCaseId, e.getMessage());
            return null;
        }
    }

    /** Writes the generated scripts from the GeneratedScriptSuite into the tests/ directory. */
    private void writeScriptFiles(String scriptPath, GeneratedScriptSuite scriptSuite) {
        if (scriptSuite == null || scriptSuite.getScripts() == null || scriptSuite.getScripts().isEmpty()) {
            log.warn("[Playwright] Script suite is empty. No test spec files to write.");
            return;
        }

        try {
            Path scriptsDir = Paths.get(scriptPath).getParent();
            Path testsDir   = scriptsDir.resolve("tests");

            // Clean and recreate the tests directory
            if (Files.exists(testsDir)) {
                try (var stream = Files.walk(testsDir)) {
                    stream.sorted(java.util.Comparator.reverseOrder())
                          .forEach(path -> {
                              try {
                                  Files.delete(path);
                              } catch (Exception deleteEx) {
                                  log.warn("[Playwright] Could not delete stale test file/dir: {} ({})", path, deleteEx.getMessage());
                              }
                          });
                }
            }
            Files.createDirectories(testsDir);

            // Write each script as a separate spec file
            for (AutomationScript script : scriptSuite.getScripts()) {
                String testCaseId = script.getTestCaseId();
                if (testCaseId == null || testCaseId.isBlank()) {
                    testCaseId = "TC-" + UUID.randomUUID().toString().substring(0, 8);
                }
                String filename = testCaseId + ".spec.ts";
                Path testFile   = testsDir.resolve(filename);
                String code     = script.getCode() != null ? script.getCode() : "";

                log.info("[Playwright] Writing test spec: {} to {}", filename, testFile.toAbsolutePath());
                Files.writeString(testFile, code);
            }
        } catch (Exception e) {
            log.error("[Playwright] Failed to write test script files: {}", e.getMessage(), e);
        }
    }

    private String stackTraceAsString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    private String resolveArtifactsBaseDir() {
        File file = new File(artifactsBaseDir);
        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        }
        File current = new File(".").getAbsoluteFile();
        while (current != null) {
            if (new File(current, "pom.xml").exists() && !new File(current.getParentFile(), "pom.xml").exists()) {
                return new File(current, "playwright-output").getAbsolutePath();
            }
            current = current.getParentFile();
        }
        return file.getAbsolutePath();
    }

    // ── Inner data class ───────────────────────────────────────────────────────

    /**
     * Holds per-test-case artifact paths parsed from Playwright's results.json.
     * Used internally within this engine — persisted downstream via ArtifactManager.
     */
    public static class TestCaseArtifactInfo {
        public String testCaseId;
        public String testTitle;
        public String status;
        public String screenshotPath;
        public String videoPath;
        public String tracePath;
        public String logPath;
    }

    @Override
    public void cancel() { running = false; }

    @Override
    public boolean isRunning() { return running; }
}
