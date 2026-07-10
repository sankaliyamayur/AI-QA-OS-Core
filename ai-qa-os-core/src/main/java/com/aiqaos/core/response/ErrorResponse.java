package com.aiqaos.core.response;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private Object details;
    private LocalDateTime timestamp;
    private String requestId;
    private String traceId;

    public ErrorResponse() {}

    public ErrorResponse(String errorCode, String errorMessage, Object details, LocalDateTime timestamp, String requestId, String traceId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.traceId = traceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String errorCode;
        private String errorMessage;
        private Object details;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String requestId;
        private String traceId;

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder details(Object details) {
            this.details = details;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(errorCode, errorMessage, details, timestamp, requestId, traceId);
        }
    }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}