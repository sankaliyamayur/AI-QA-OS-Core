package com.aiqaos.integration.facade;

import com.aiqaos.core.engine.QABrainEngine;
import com.aiqaos.core.engine.ReportingEngine;
import com.aiqaos.core.engine.WorkflowEngine;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.BrainRequest;
import com.aiqaos.core.contract.BrainResponse;
import com.aiqaos.core.contract.ReportRequest;
import com.aiqaos.core.contract.ReportResponse;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.memory.retrieval.MemoryRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * PlatformIntegrationFacade — central integration point for the Autonomous QA Platform.
 * Orchestrates Brain, Memory, Workflow, and Reporting engines through their core contracts.
 */
@Component
@SuppressWarnings("unchecked")
public class PlatformIntegrationFacade {

    private final QABrainEngine brainEngine;
    private final MemoryRetriever memoryRetriever;
    private final WorkflowEngine workflowEngine;
    private final ReportingEngine reportingEngine;

    public PlatformIntegrationFacade(
            QABrainEngine brainEngine,
            MemoryRetriever memoryRetriever,
            @Qualifier("workflowManagerImpl") WorkflowEngine workflowEngine,
            @Qualifier("reportManagerImpl") ReportingEngine reportingEngine) {
        this.brainEngine = brainEngine;
        this.memoryRetriever = memoryRetriever;
        this.workflowEngine = workflowEngine;
        this.reportingEngine = reportingEngine;
    }

    public BrainResponse analyze(BrainRequest req) {
        return brainEngine.processBrainDecision(req);
    }

    public com.aiqaos.memory.model.VectorSearchResult retrieveMemory(String query) {
        java.util.List<com.aiqaos.memory.model.VectorSearchResult> results =
                memoryRetriever.retrieveSimilar(query, 1, "knowledge", null);
        return results.isEmpty() ? null : results.get(0);
    }

    public WorkflowResponse executeWorkflow(WorkflowRequest req) {
        return (WorkflowResponse) workflowEngine.executeWorkflow(req, new WorkflowContext());
    }

    public ReportResponse compileReport(ReportRequest req) {
        return (ReportResponse) reportingEngine.generateReport(req);
    }
}