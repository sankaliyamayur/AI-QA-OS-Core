package com.aiqaos.core.response;

import java.time.LocalDateTime;

public class BaseResponse<T> {
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String requestId;
    private String traceId;

    public BaseResponse() {}

    public BaseResponse(String status, String message, T data, LocalDateTime timestamp, String requestId, String traceId) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.traceId = traceId;
    }

    public static <T> BaseResponse<T> success(T data, String requestId, String traceId) {
        return new BaseResponse<>("SUCCESS", "Operation completed successfully", data, LocalDateTime.now(), requestId, traceId);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private String status;
        private String message;
        private T data;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String requestId;
        private String traceId;

        public Builder<T> status(String status) {
            this.status = status;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<T> requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder<T> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public BaseResponse<T> build() {
            return new BaseResponse<>(status, message, data, timestamp, requestId, traceId);
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}