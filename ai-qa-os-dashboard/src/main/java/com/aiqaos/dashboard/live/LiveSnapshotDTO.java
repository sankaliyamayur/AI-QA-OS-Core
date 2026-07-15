package com.aiqaos.dashboard.live;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LiveSnapshotDTO {
    private double cpuLoadPercent;
    private long memoryUsedMb;
    private long memoryMaxMb;
    private int activeQueueDepth;
    private List<UUID> activeWorkflowIds;
    private List<LiveCostPointDTO> liveLlmCosts;
    private LocalDateTime timestamp;

    public double getCpuLoadPercent() { return cpuLoadPercent; }
    public void setCpuLoadPercent(double cpuLoadPercent) { this.cpuLoadPercent = cpuLoadPercent; }
    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }
    public long getMemoryMaxMb() { return memoryMaxMb; }
    public void setMemoryMaxMb(long memoryMaxMb) { this.memoryMaxMb = memoryMaxMb; }
    public int getActiveQueueDepth() { return activeQueueDepth; }
    public void setActiveQueueDepth(int activeQueueDepth) { this.activeQueueDepth = activeQueueDepth; }
    public List<UUID> getActiveWorkflowIds() { return activeWorkflowIds; }
    public void setActiveWorkflowIds(List<UUID> activeWorkflowIds) { this.activeWorkflowIds = activeWorkflowIds; }
    public List<LiveCostPointDTO> getLiveLlmCosts() { return liveLlmCosts; }
    public void setLiveLlmCosts(List<LiveCostPointDTO> liveLlmCosts) { this.liveLlmCosts = liveLlmCosts; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
