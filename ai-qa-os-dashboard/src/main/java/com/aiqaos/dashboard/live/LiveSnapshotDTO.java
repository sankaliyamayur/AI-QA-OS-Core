package com.aiqaos.dashboard.live;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LiveSnapshotDTO {
    private double cpuLoadPercent;
    private double cpuUsage;
    private long memoryUsedMb;
    private long memoryMaxMb;
    private double memoryUsage;
    private int activeQueueDepth;
    private int queueSize;
    private int runningPipelines;
    private int activeAgents;
    private boolean redisConnected;
    private int dbPoolActive;
    private int tokensPerSec;
    private int requestsPerSec;
    private int avgLatencyMs;
    private List<UUID> activeWorkflowIds;
    private List<LiveCostPointDTO> liveLlmCosts;
    private LocalDateTime timestamp;

    public double getCpuLoadPercent() { return cpuLoadPercent; }
    public void setCpuLoadPercent(double cpuLoadPercent) { 
        this.cpuLoadPercent = cpuLoadPercent; 
        this.cpuUsage = Math.round(cpuLoadPercent * 10.0) / 10.0;
    }

    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }

    public long getMemoryMaxMb() { return memoryMaxMb; }
    public void setMemoryMaxMb(long memoryMaxMb) { this.memoryMaxMb = memoryMaxMb; }

    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }

    public int getActiveQueueDepth() { return activeQueueDepth; }
    public void setActiveQueueDepth(int activeQueueDepth) { 
        this.activeQueueDepth = activeQueueDepth; 
        this.queueSize = activeQueueDepth;
    }

    public int getQueueSize() { return queueSize; }
    public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

    public int getRunningPipelines() { return runningPipelines; }
    public void setRunningPipelines(int runningPipelines) { this.runningPipelines = runningPipelines; }

    public int getActiveAgents() { return activeAgents; }
    public void setActiveAgents(int activeAgents) { this.activeAgents = activeAgents; }

    public boolean isRedisConnected() { return redisConnected; }
    public void setRedisConnected(boolean redisConnected) { this.redisConnected = redisConnected; }

    public int getDbPoolActive() { return dbPoolActive; }
    public void setDbPoolActive(int dbPoolActive) { this.dbPoolActive = dbPoolActive; }

    public int getTokensPerSec() { return tokensPerSec; }
    public void setTokensPerSec(int tokensPerSec) { this.tokensPerSec = tokensPerSec; }

    public int getRequestsPerSec() { return requestsPerSec; }
    public void setRequestsPerSec(int requestsPerSec) { this.requestsPerSec = requestsPerSec; }

    public int getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(int avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public List<UUID> getActiveWorkflowIds() { return activeWorkflowIds; }
    public void setActiveWorkflowIds(List<UUID> activeWorkflowIds) { this.activeWorkflowIds = activeWorkflowIds; }

    public List<LiveCostPointDTO> getLiveLlmCosts() { return liveLlmCosts; }
    public void setLiveLlmCosts(List<LiveCostPointDTO> liveLlmCosts) { this.liveLlmCosts = liveLlmCosts; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
