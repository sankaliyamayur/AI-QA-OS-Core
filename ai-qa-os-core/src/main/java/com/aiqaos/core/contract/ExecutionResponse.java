package com.aiqaos.core.contract;

public class ExecutionResponse extends BaseResponse {
    private int exitCode;
    private String stdout;
    private String stderr;
    private long executionTimeMillis;

    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    public String getStdout() { return stdout; }
    public void setStdout(String stdout) { this.stdout = stdout; }
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    public long getExecutionTimeMillis() { return executionTimeMillis; }
    public void setExecutionTimeMillis(long executionTimeMillis) { this.executionTimeMillis = executionTimeMillis; }
}