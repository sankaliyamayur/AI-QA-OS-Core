package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.ArtifactDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ArtifactController
 *
 * Provides two groups of endpoints:
 *
 * 1. Metadata API  — returns artifact URLs for a test case
 *    GET  /api/dashboard/artifacts/{testCaseId}
 *         → ArtifactDTO (latest run)
 *    GET  /api/dashboard/artifacts/{testCaseId}/history
 *         → List<ArtifactDTO.RunEntry> (all historical runs)
 *
 * 2. File serving  — serves raw artifact files (screenshot, video, trace, report)
 *    GET  /api/artifacts/**
 *         → streams the file from the artifact base directory
 *
 * The frontend Vite proxy forwards /api → http://localhost:8090, so all these
 * endpoints are reachable from the React UI without CORS concerns.
 */
@RestController
public class ArtifactController {

    private static final Logger log = LoggerFactory.getLogger(ArtifactController.class);

    private final String resolvedBaseDir;

    /** Dashboard public base URL used to build HTTP URLs from file paths */
    @Value("${aiqaos.dashboard.base-url:http://localhost:8090}")
    private String dashboardBaseUrl;

    private final JdbcTemplate jdbc;

    public ArtifactController(JdbcTemplate jdbc, 
                              @Value("${aiqaos.artifacts.base-dir:./playwright-output}") String artifactsBaseDir) {
        this.jdbc = jdbc;
        File file = new File(artifactsBaseDir);
        if (file.isAbsolute()) {
            this.resolvedBaseDir = file.getAbsolutePath();
        } else {
            File current = new File(".").getAbsoluteFile();
            String rootPath = file.getAbsolutePath();
            while (current != null) {
                if (new File(current, "pom.xml").exists() && !new File(current.getParentFile(), "pom.xml").exists()) {
                    rootPath = new File(current, "playwright-output").getAbsolutePath();
                    break;
                }
                current = current.getParentFile();
            }
            this.resolvedBaseDir = rootPath;
        }
        log.info("[ArtifactController] Resolved artifacts base directory to: {}", this.resolvedBaseDir);
    }

    // ── 1. Metadata: latest artifact record ──────────────────────────────────

    /**
     * Returns the latest Playwright artifact record for a test case.
     * Returns 404 if no artifacts have been registered (test has never failed,
     * or Playwright has not been run yet).
     *
     * @param testCaseId e.g. "TC-AL-003"
     */
    @GetMapping("/api/dashboard/artifacts/{testCaseId}")
    public ResponseEntity<ArtifactDTO> getArtifacts(@PathVariable("testCaseId") String testCaseId) {
        log.debug("Fetching latest artifacts for testCaseId={}", testCaseId);

        List<Map<String, Object>> rows = jdbc.queryForList(
            """
            SELECT ea.run_number, ea.execution_id::text AS execution_id, ea.test_case_id,
                   ea.browser AS browser, ea.screenshot_path, ea.video_path, ea.trace_path,
                   ea.report_path, ea.log_path, e.status AS exec_status
            FROM execution_artifacts ea
            LEFT JOIN executions e ON e.execution_id = ea.execution_id::uuid
            WHERE ea.test_case_id = ?
            ORDER BY ea.run_number DESC
            LIMIT 1
            """,
            testCaseId
        );

        if (rows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ArtifactDTO dto = mapRowToDto(rows.get(0));
        dto.setHistory(buildHistory(testCaseId));
        return ResponseEntity.ok(dto);
    }

    // ── 2. Execution history for a test case ─────────────────────────────────

    /**
     * Returns all historical runs for a test case ordered oldest → newest.
     * Enables the "Run #1 / Run #2 / Run #3" timeline in the dashboard.
     */
    @GetMapping("/api/dashboard/artifacts/{testCaseId}/history")
    public ResponseEntity<List<ArtifactDTO.RunEntry>> getArtifactHistory(@PathVariable("testCaseId") String testCaseId) {
        log.debug("Fetching artifact history for testCaseId={}", testCaseId);
        List<ArtifactDTO.RunEntry> history = buildHistory(testCaseId);
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history);
    }

    // ── 3. File serving ───────────────────────────────────────────────────────

    /**
     * Serves raw Playwright artifact files (screenshots, videos, traces, HTML reports).
     *
     * Example: GET /api/artifacts/exec-abc123/chromium/test-results/TC-AL-003/screenshot.png
     *
     * Security: only files under the configured artifactsBaseDir are accessible.
     * Path traversal attempts (containing "..") are rejected with 400.
     */
    @GetMapping("/api/artifacts/**")
    public ResponseEntity<Resource> serveArtifact(HttpServletRequest request) throws IOException {
        String requestPath = request.getRequestURI()
            .substring(request.getContextPath().length() + "/api/artifacts/".length());

        // ── Security: reject path traversal attempts ──────────────────────────
        if (requestPath.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(resolvedBaseDir).resolve(requestPath).normalize();
        File file     = filePath.toFile();

        // ── Check the resolved path is still within the base dir ─────────────
        if (!filePath.startsWith(Paths.get(resolvedBaseDir).normalize())) {
            log.warn("Attempted path traversal: {}", requestPath);
            return ResponseEntity.badRequest().build();
        }

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // ── Detect content type ───────────────────────────────────────────────
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            if (requestPath.endsWith(".webm"))  contentType = "video/webm";
            else if (requestPath.endsWith(".zip")) contentType = "application/zip";
            else if (requestPath.endsWith(".png")) contentType = "image/png";
            else if (requestPath.endsWith(".html")) contentType = "text/html";
            else contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
            .body(resource);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ArtifactDTO mapRowToDto(Map<String, Object> row) {
        ArtifactDTO dto = new ArtifactDTO();
        dto.setTestCaseId((String) row.get("test_case_id"));
        dto.setBrowser((String) row.get("browser"));
        dto.setStatus(row.get("exec_status") != null
            ? (String) row.get("exec_status")
            : "unknown");

        dto.setScreenshotUrl(toArtifactUrl((String) row.get("screenshot_path")));
        dto.setVideoUrl(toArtifactUrl((String) row.get("video_path")));
        dto.setTraceUrl(toArtifactUrl((String) row.get("trace_path")));
        dto.setHtmlReportUrl(toArtifactUrl((String) row.get("report_path")));

        // Read log file contents inline (small files, typically < 10 KB)
        String logPath = (String) row.get("log_path");
        if (logPath != null) {
            dto.setConsoleLog(readFileSafe(logPath));
        }

        return dto;
    }

    private List<ArtifactDTO.RunEntry> buildHistory(String testCaseId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            """
            SELECT ea.run_number, ea.execution_id::text AS execution_id,
                   ea.browser AS browser, ea.screenshot_path, ea.video_path, ea.trace_path,
                   e.status AS exec_status
            FROM execution_artifacts ea
            LEFT JOIN executions e ON e.execution_id = ea.execution_id::uuid
            WHERE ea.test_case_id = ?
            ORDER BY ea.run_number ASC
            """,
            testCaseId
        );

        List<ArtifactDTO.RunEntry> history = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ArtifactDTO.RunEntry entry = new ArtifactDTO.RunEntry();
            entry.setRunNumber(row.get("run_number") != null ? (int) row.get("run_number") : 0);
            entry.setExecutionId((String) row.get("execution_id"));
            entry.setBrowser((String) row.get("browser"));
            entry.setStatus(row.get("exec_status") != null ? (String) row.get("exec_status") : "unknown");
            entry.setScreenshotUrl(toArtifactUrl((String) row.get("screenshot_path")));
            entry.setVideoUrl(toArtifactUrl((String) row.get("video_path")));
            entry.setTraceUrl(toArtifactUrl((String) row.get("trace_path")));
            history.add(entry);
        }
        return history;
    }

    /**
     * Converts an absolute filesystem path to an HTTP URL served via /api/artifacts/.
     * Returns null if the path is null or the file does not exist.
     */
    private String toArtifactUrl(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) return null;
        File file = new File(absolutePath);
        if (!file.exists()) return null;

        try {
            Path base    = Paths.get(resolvedBaseDir).normalize().toAbsolutePath();
            Path target  = Paths.get(absolutePath).normalize().toAbsolutePath();
            String rel   = base.relativize(target).toString().replace("\\", "/");
            return dashboardBaseUrl + "/api/artifacts/" + rel;
        } catch (Exception e) {
            log.warn("Could not convert path to URL: {}", absolutePath);
            return null;
        }
    }

    /** Reads a text file safely, returning null on any error. */
    private String readFileSafe(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (Exception e) {
            log.warn("Could not read log file: {} ({})", path, e.getMessage());
            return null;
        }
    }
}
