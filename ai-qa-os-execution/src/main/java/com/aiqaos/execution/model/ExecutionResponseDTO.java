package com.aiqaos.execution.model;

import com.aiqaos.core.dto.BaseDTO;

public class ExecutionResponseDTO implements BaseDTO {
    private String status;
    private String message;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}