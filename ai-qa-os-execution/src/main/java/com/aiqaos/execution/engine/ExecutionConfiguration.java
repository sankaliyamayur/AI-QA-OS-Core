package com.aiqaos.execution.engine;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionConfiguration {
    private ExecutionMode executionMode = ExecutionMode.SEQUENTIAL;
    private EnvironmentType environment = EnvironmentType.DEV;
    private int timeout = 30000;
    private int retryCount = 0;
    private BrowserType browser = BrowserType.CHROME;
    private boolean headless = true;

    // WF-4: the browser matrix to fan out across (empty → just [browser], the pre-WF-4 behaviour).
    private java.util.List<BrowserType> browsers = new java.util.ArrayList<>();
    // WF-4: total Playwright shards for the suite (1 = no sharding). Per-unit index below.
    private int shardCount = 1;
    // WF-4: which shard THIS unit runs (1-based; 0 = not sharded). Set by the matrix planner per unit.
    private int shardIndex = 0;

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

    public java.util.List<BrowserType> getBrowsers() { return browsers; }
    public void setBrowsers(java.util.List<BrowserType> browsers) {
        this.browsers = browsers == null ? new java.util.ArrayList<>() : browsers;
    }

    public int getShardCount() { return shardCount; }
    public void setShardCount(int shardCount) { this.shardCount = shardCount; }

    public int getShardIndex() { return shardIndex; }
    public void setShardIndex(int shardIndex) { this.shardIndex = shardIndex; }
}
