package com.aiqaos.core.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExecutionResult {
    private String taskId;
    private String agentId;
    private boolean success;
    private String outputData;
    private String errorMessage;
    private LocalDateTime completedAt = LocalDateTime.now();
}
