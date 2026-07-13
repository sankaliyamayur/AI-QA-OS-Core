package com.aiqaos.integration.context;

import com.aiqaos.integration.dto.PlatformExecutionSummary;

public class IntegrationResult {
    private final String status;
    private final String message;
    private final PlatformExecutionSummary summary;

    public IntegrationResult(String status, String message, PlatformExecutionSummary summary) {
        this.status = status;
        this.message = message;
        this.summary = summary;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public PlatformExecutionSummary getSummary() { return summary; }
}