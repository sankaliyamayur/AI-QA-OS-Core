package com.aiqaos.execution.engine;

public class ExecutionConfiguration {
    private ExecutionMode executionMode = ExecutionMode.SEQUENTIAL;
    private EnvironmentType environment = EnvironmentType.DEV;
    private int timeout = 30000;
    private int retryCount = 0;
    private BrowserType browser = BrowserType.CHROME;
    private boolean headless = true;

    public ExecutionConfiguration() {}

    public ExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(ExecutionMode executionMode) { this.executionMode = executionMode; }

    public EnvironmentType getEnvironment() { return environment; }
    public void setEnvironment(EnvironmentType environment) { this.environment = environment; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public BrowserType getBrowser() { return browser; }
    public void setBrowser(BrowserType browser) { this.browser = browser; }

    public boolean isHeadless() { return headless; }
    public void setHeadless(boolean headless) { this.headless = headless; }
}
