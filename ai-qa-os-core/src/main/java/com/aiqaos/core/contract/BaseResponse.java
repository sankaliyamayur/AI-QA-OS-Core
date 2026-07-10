package com.aiqaos.core.contract;

import com.aiqaos.core.dto.BaseDTO;

public abstract class BaseResponse implements BaseDTO {
    private BaseMetadata metadata = new BaseMetadata();
    private String status;
    private String message;

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}