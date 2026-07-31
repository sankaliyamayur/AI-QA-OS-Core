package com.aiqaos.execution.queue;

import com.aiqaos.core.model.ExecutionResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * SCALE-1: the outcome of a queued {@link ExecutionJob}. Carries the {@link ExecutionResult} on
 * success, plus {@code artifactKeys} — {@link com.aiqaos.execution.artifact.ArtifactStore} keys
 * rather than host paths, so a cross-host worker's artifacts are addressable once object storage
 * (ENT-5) backs the store. Empty today (the local worker still uses the engine's on-disk paths).
 */
public class ExecutionJobResult {

    private final String jobId;
    private final boolean success;
    private final ExecutionResult result;
    private final List<String> artifactKeys;
    private final String errorMessage;

    @JsonCreator
    private ExecutionJobResult(@JsonProperty("jobId") String jobId,
                               @JsonProperty("success") boolean success,
                               @JsonProperty("result") ExecutionResult result,
                               @JsonProperty("artifactKeys") List<String> artifactKeys,
                               @JsonProperty("errorMessage") String errorMessage) {
        this.jobId = jobId;
        this.success = success;
        this.result = result;
        this.artifactKeys = artifactKeys == null ? List.of() : List.copyOf(artifactKeys);
        this.errorMessage = errorMessage;
    }

    public static ExecutionJobResult success(String jobId, ExecutionResult result) {
        return new ExecutionJobResult(jobId, true, result, List.of(), null);
    }

    public static ExecutionJobResult success(String jobId, ExecutionResult result, List<String> artifactKeys) {
        return new ExecutionJobResult(jobId, true, result, artifactKeys, null);
    }

    public static ExecutionJobResult failure(String jobId, String errorMessage) {
        return new ExecutionJobResult(jobId, false, null, List.of(), errorMessage);
    }

    public String getJobId() {
        return jobId;
    }

    public boolean isSuccess() {
        return success;
    }

    public ExecutionResult getResult() {
        return result;
    }

    public List<String> getArtifactKeys() {
        return artifactKeys;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
