package com.aiqaos.execution.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class ExecutionRequestDTO implements BaseDTO {
    private UUID executionId;
    private String scriptCommand;
    private String provider;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getScriptCommand() { return scriptCommand; }
    public void setScriptCommand(String scriptCommand) { this.scriptCommand = scriptCommand; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}