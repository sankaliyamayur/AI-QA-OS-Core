package com.aiqaos.reporting.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class ReportRequestDTO implements BaseDTO {
    private UUID executionId;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
}