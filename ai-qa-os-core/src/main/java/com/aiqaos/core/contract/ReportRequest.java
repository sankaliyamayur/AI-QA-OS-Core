package com.aiqaos.core.contract;

import java.util.UUID;

public class ReportRequest extends BaseRequest {
    private UUID executionId;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
}
