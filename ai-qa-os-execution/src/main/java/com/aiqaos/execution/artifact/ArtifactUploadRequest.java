package com.aiqaos.execution.artifact;

import java.util.UUID;

/**
 * FI-ENT5-A (ADR-071): the inputs to upload one execution's produced artifact files into the durable
 * {@link ArtifactStore}. The object key for each file is derived deterministically from these fields
 * (see {@link ArtifactUploader#keyFor}), so nothing extra is persisted — the key can be reconstructed
 * to resolve the durable copy later.
 *
 * @param executionId    the execution the artifacts belong to
 * @param testCaseId     human-readable test case id (e.g. "TC-AL-003")
 * @param browser        chromium | firefox | webkit
 * @param runNumber      1-based execution-history index
 * @param screenshotPath local file path, or null if none produced
 * @param videoPath      local file path, or null
 * @param tracePath      local file path, or null
 * @param logPath        local file path, or null
 * @param reportPath     local file path, or null
 */
public record ArtifactUploadRequest(
        UUID executionId,
        String testCaseId,
        String browser,
        int runNumber,
        String screenshotPath,
        String videoPath,
        String tracePath,
        String logPath,
        String reportPath) {
}
