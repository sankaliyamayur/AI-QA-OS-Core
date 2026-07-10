package com.aiqaos.core.event;

public class ExecutionEvent extends BaseEvent {
    private String scriptExecuted;
    private int statusResult;

    public String getScriptExecuted() { return scriptExecuted; }
    public void setScriptExecuted(String scriptExecuted) { this.scriptExecuted = scriptExecuted; }
    public int getStatusResult() { return statusResult; }
    public void setStatusResult(int statusResult) { this.statusResult = statusResult; }
}