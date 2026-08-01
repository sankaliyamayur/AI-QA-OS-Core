package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.ArtifactDTO;
import com.aiqaos.execution.artifact.ArtifactStore;
import com.aiqaos.execution.artifact.ArtifactUploadRequest;
import com.aiqaos.execution.artifact.ArtifactUploader;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.UUID;

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

    /** FI-ENT5-C (ADR-073): when durable upload is on, emit store-URLs that resolve from ArtifactStore. */
    @Value("${aiqaos.artifacts.upload.enabled:false}")
    private boolean durableArtifacts;

    private final JdbcTemplate jdbc;

    /** FI-ENT5-C: the durable store (Local or Object); present as a bean, injected via provider. */
    private final ObjectProvider<ArtifactStore> artifactStoreProvider;

    public ArtifactController(JdbcTemplate jdbc,
                              ObjectProvider<ArtifactStore> artifactStoreProvider,
                              @Value("${aiqaos.artifacts.base-dir:./playwright-output}") String artifactsBaseDir) {
        this.jdbc = jdbc;
        this.artifactStoreProvider = artifactStoreProvider;
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

        // ── SEC-4: real-path check — a symlink inside the base must not resolve outside it ─────
        try {
            Path realBase = Paths.get(resolvedBaseDir).toRealPath();
            if (!filePath.toRealPath().startsWith(realBase)) {
                log.warn("Attempted real-path/symlink escape: {}", requestPath);
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            log.warn("Could not resolve real path for artifact: {}", requestPath);
            return ResponseEntity.badRequest().build();
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

        // ── SEC-4: HTML reports are interactive JS apps — serve them as a download so a report that
        //    echoes user data cannot execute in the app origin. Media (image/video/trace) stays inline.
        boolean isHtml = contentType.toLowerCase().contains("text/html");
        String disposition = (isHtml ? "attachment" : "inline") + "; filename=\"" + file.getName() + "\"";

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            // SEC-4: a served artifact never needs to load app resources or run scripts.
            .header("X-Content-Type-Options", "nosniff")
            .header("Content-Security-Policy", "default-src 'none'; sandbox")
            .body(resource);
    }

    // ── 4. Durable serving from ArtifactStore (FI-ENT5-C) ─────────────────────

    /**
     * FI-ENT5-C (ADR-073): serves an artifact's bytes from the durable {@link ArtifactStore} by key
     * (the {@link ArtifactUploader#keyFor} scheme uploaded by FI-ENT5-A), so artifacts are reachable
     * cross-host / after local cleanup. Resolution uses the request's bound tenant (system on the open
     * dashboard) — correct for single-tenant/system deployments; multi-tenant serve-binding is a
     * follow-on (FI-ENT5-E). Same SEC-4 hardening as file serving.
     *
     * Example: GET /api/artifacts/store/executions/&lt;id&gt;/run-2/chromium/TC-AL-003/screenshot
     */
    @GetMapping("/api/artifacts/store/**")
    public ResponseEntity<Resource> serveFromStore(HttpServletRequest request) {
        String key = request.getRequestURI()
            .substring(request.getContextPath().length() + "/api/artifacts/store/".length());

        // Security: reject traversal — defence-in-depth on top of ArtifactStore's own key guard.
        if (key.isBlank() || key.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        ArtifactStore store = artifactStoreProvider.getIfAvailable();
        if (store == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes;
        try {
            bytes = store.resolve(key);
        } catch (Exception e) {
            // resolve() throws when the key is absent (e.g. Local/Object store) — treat as 404.
            return ResponseEntity.notFound().build();
        }
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = contentTypeForKey(key);
        boolean isHtml = contentType.toLowerCase().contains("text/html");
        String fileName = key.substring(key.lastIndexOf('/') + 1);
        String disposition = (isHtml ? "attachment" : "inline") + "; filename=\"" + fileName + "\"";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            // SEC-4: a served artifact never needs to load app resources or run scripts.
            .header("X-Content-Type-Options", "nosniff")
            .header("Content-Security-Policy", "default-src 'none'; sandbox")
            .body(new ByteArrayResource(bytes));
    }

    /** Content type for a durable key, from its trailing {@code /<type>} segment (FI-ENT5-A scheme). */
    static String contentTypeForKey(String key) {
        String type = key.substring(key.lastIndexOf('/') + 1);
        switch (type) {
            case "screenshot": return "image/png";
            case "video":      return "video/webm";
            case "trace":      return "application/zip";
            case "report":     return "text/html";
            case "log":        return "text/plain";
            default:           return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ArtifactDTO mapRowToDto(Map<String, Object> row) {
        ArtifactDTO dto = new ArtifactDTO();
        dto.setTestCaseId((String) row.get("test_case_id"));
        dto.setBrowser((String) row.get("browser"));
        dto.setStatus(row.get("exec_status") != null
            ? (String) row.get("exec_status")
            : "unknown");

        dto.setScreenshotUrl(artifactUrl(row, "screenshot", (String) row.get("screenshot_path")));
        dto.setVideoUrl(artifactUrl(row, "video", (String) row.get("video_path")));
        dto.setTraceUrl(artifactUrl(row, "trace", (String) row.get("trace_path")));
        dto.setHtmlReportUrl(artifactUrl(row, "report", (String) row.get("report_path")));

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
            SELECT ea.run_number, ea.execution_id::text AS execution_id, ea.test_case_id,
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
            entry.setScreenshotUrl(artifactUrl(row, "screenshot", (String) row.get("screenshot_path")));
            entry.setVideoUrl(artifactUrl(row, "video", (String) row.get("video_path")));
            entry.setTraceUrl(artifactUrl(row, "trace", (String) row.get("trace_path")));
            history.add(entry);
        }
        return history;
    }

    /**
     * FI-ENT5-C (ADR-073): picks the artifact URL. When durable upload is enabled and the artifact was
     * produced (its local path column is non-null), emit the durable store-URL (resolves regardless of
     * local presence — cross-host); otherwise the local-file URL, unchanged.
     */
    private String artifactUrl(Map<String, Object> row, String type, String localPath) {
        if (durableArtifacts && localPath != null && !localPath.isBlank()) {
            String executionId = (String) row.get("execution_id");
            int runNumber = row.get("run_number") != null ? ((Number) row.get("run_number")).intValue() : 1;
            String browser = (String) row.get("browser");
            String testCaseId = (String) row.get("test_case_id");
            String durable = durableUrl(executionId, runNumber, browser, testCaseId, type);
            if (durable != null) {
                return durable;
            }
        }
        return toArtifactUrl(localPath);
    }

    /** Builds the durable store-URL for an artifact using FI-ENT5-A's deterministic key scheme. */
    private String durableUrl(String executionId, int runNumber, String browser, String testCaseId, String type) {
        if (executionId == null || executionId.isBlank()) {
            return null;
        }
        try {
            String key = ArtifactUploader.keyFor(new ArtifactUploadRequest(
                    UUID.fromString(executionId), testCaseId, browser, runNumber,
                    null, null, null, null, null), type);
            return dashboardBaseUrl + "/api/artifacts/store/" + key;
        } catch (Exception e) {
            log.warn("Could not build durable artifact URL for {} {}: {}", executionId, type, e.getMessage());
            return null;
        }
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
