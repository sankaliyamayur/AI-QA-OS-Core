package com.aiqaos.execution.artifact;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * FI-ENT5-A (ADR-071): uploads an execution's produced artifact files (screenshot / video / trace /
 * log / report) into the durable {@link ArtifactStore}. With the S3/MinIO binding active (ADR-068)
 * the bytes land in shared object storage — cross-host reachable — closing SCALE-1's artifact gap at
 * the producer.
 *
 * <p>Opt-in ({@code aiqaos.artifacts.upload.enabled=true}) so the default execution path is unchanged.
 * <b>Best-effort:</b> a missing file or a store failure is logged and skipped — artifact upload must
 * never fail an execution. The tenant-key prefix (ADR-056) is applied by {@code ObjectStorageArtifactStore}
 * from the thread's bound tenant, so no tenant handling is needed here.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.upload.enabled", havingValue = "true")
public class ArtifactUploader {

    private static final Logger log = LoggerFactory.getLogger(ArtifactUploader.class);

    private final ArtifactStore artifactStore;

    public ArtifactUploader(ArtifactStore artifactStore) {
        this.artifactStore = artifactStore;
    }

    /** Uploads every non-null, existing file in the request; returns the keys actually stored. */
    public List<String> upload(ArtifactUploadRequest req) {
        List<String> storedKeys = new ArrayList<>();
        if (req == null) {
            return storedKeys;
        }
        uploadOne(req, "screenshot", req.screenshotPath(), storedKeys);
        uploadOne(req, "video", req.videoPath(), storedKeys);
        uploadOne(req, "trace", req.tracePath(), storedKeys);
        uploadOne(req, "log", req.logPath(), storedKeys);
        uploadOne(req, "report", req.reportPath(), storedKeys);
        return storedKeys;
    }

    private void uploadOne(ArtifactUploadRequest req, String type, String path, List<String> storedKeys) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            Path file = Path.of(path);
            if (!Files.isRegularFile(file)) {
                log.debug("FI-ENT5-A: skipping {} artifact — no file at {}", type, path);
                return;
            }
            byte[] bytes = Files.readAllBytes(file);
            String key = keyFor(req, type);
            artifactStore.store(key, bytes);
            storedKeys.add(key);
        } catch (Exception ex) {
            // Best-effort: never let artifact upload break an execution.
            log.warn("FI-ENT5-A: could not upload {} artifact {}: {}", type, path, ex.getMessage());
        }
    }

    /**
     * The deterministic object key for an artifact — a pure function of the request's stable fields, so
     * the durable copy can be reconstructed and resolved without persisting the key.
     * {@code executions/<executionId>/run-<n>/<browser>/<testCaseId>/<type>}.
     */
    public static String keyFor(ArtifactUploadRequest req, String type) {
        return "executions/" + safe(req.executionId())
                + "/run-" + req.runNumber()
                + "/" + safe(req.browser())
                + "/" + safe(req.testCaseId())
                + "/" + type;
    }

    private static String safe(Object value) {
        String s = (value == null) ? null : value.toString();
        return (s == null || s.isBlank()) ? "unknown" : s;
    }
}
