package com.aiqaos.core.contract;

import java.io.Serializable;

public class BaseResult<T> implements Serializable {
    private BaseMetadata metadata = new BaseMetadata();
    private boolean success;
    private T data;
    private String errorCode;
    private String errorMessage;

    public BaseMetadata getMetadata() { return metadata; }
    public void setMetadata(BaseMetadata metadata) { this.metadata = metadata; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}