package com.aiqaos.dashboard.live;

import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import com.aiqaos.workflow.repository.WorkflowExecutionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;

@Component
public class LiveMetricsPoller {

    private final SseBroadcaster broadcaster;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final LLMCostRepository llmCostRepository;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public LiveMetricsPoller(SseBroadcaster broadcaster,
                              WorkflowExecutionRepository workflowExecutionRepository,
                              LLMCostRepository llmCostRepository) {
        this.broadcaster = broadcaster;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.llmCostRepository = llmCostRepository;
    }

    @Scheduled(fixedRate = 3000)
    public void pollAndBroadcast() {
        LiveSnapshotDTO snapshot = new LiveSnapshotDTO();
        snapshot.setCpuLoadPercent(readCpuLoadPercent());

        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        snapshot.setMemoryUsedMb(heap.getUsed() / (1024 * 1024));
        snapshot.setMemoryMaxMb(heap.getMax() > 0 ? heap.getMax() / (1024 * 1024) : -1);

        java.util.List<WorkflowExecutionEntity> running = workflowExecutionRepository.findByStatus("RUNNING");
        snapshot.setActiveWorkflowIds(running.stream().map(WorkflowExecutionEntity::getWorkflowId).toList());
        snapshot.setActiveQueueDepth(running.size());

        snapshot.setLiveLlmCosts(llmCostRepository.findTop20ByOrderByTimestampDesc().stream()
                .map(this::toCostPoint)
                .toList());
        snapshot.setTimestamp(LocalDateTime.now());

        broadcaster.broadcast("live-metrics", snapshot);
    }

    private LiveCostPointDTO toCostPoint(LLMCostEntity entity) {
        return new LiveCostPointDTO(entity.getProvider(), entity.getModel(), entity.getCost(), entity.getTimestamp());
    }

    private double readCpuLoadPercent() {
        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                double load = sunBean.getCpuLoad();
                return load < 0 ? -1 : load * 100.0;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
