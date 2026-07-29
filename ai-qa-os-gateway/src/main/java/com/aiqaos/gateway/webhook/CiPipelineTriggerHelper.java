package com.aiqaos.gateway.webhook;

import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * WF-2: CI Trigger & Webhook Helper.
 * Dispatches incoming CI events (GitHub, GitLab, Jenkins, Azure DevOps) to the pipeline.
 */
@Component
public class CiPipelineTriggerHelper {

    private static final Logger log = LoggerFactory.getLogger(CiPipelineTriggerHelper.class);

    private final ObjectProvider<AutonomousQAPipelineOrchestrator> orchestratorProvider;

    public CiPipelineTriggerHelper(ObjectProvider<AutonomousQAPipelineOrchestrator> orchestratorProvider) {
        this.orchestratorProvider = orchestratorProvider;
    }

    public WorkflowResponse triggerCiWorkflow(String ciSource, String branch, String commitHash, String repoName, String mode) {
        log.info("WF-2: Triggering CI autonomous QA pipeline from {}. Branch: {}, Commit: {}, Repo: {}, Mode: {}",
                ciSource, branch, commitHash, repoName, mode);

        WorkflowRequest req = new WorkflowRequest();
        req.setWorkflowName("AUTONOMOUS_QA_PIPELINE");
        req.getInputs().put("ciSource", ciSource);
        req.getInputs().put("gitBranch", branch != null ? branch : "main");
        req.getInputs().put("gitCommit", commitHash != null ? commitHash : "HEAD");
        req.getInputs().put("repository", repoName != null ? repoName : "unknown");
        req.getInputs().put("workflowMode", mode != null ? mode : "FULL_E2E");

        WorkflowContext ctx = new WorkflowContext();
        ctx.getMetadata().setCorrelationId(UUID.randomUUID());

        AutonomousQAPipelineOrchestrator orchestrator = orchestratorProvider.getIfAvailable();
        if (orchestrator != null) {
            return orchestrator.runPipeline(req, ctx);
        } else {
            log.info("WF-2: Orchestrator not active in current profile. Simulating CI workflow trigger.");
            WorkflowResponse resp = new WorkflowResponse();
            resp.setStatus("SUCCESS");
            resp.setMessage("Simulated CI trigger for " + ciSource + " (" + branch + ")");
            resp.getOutputs().put("ciSource", ciSource);
            resp.getOutputs().put("branch", branch);
            resp.getOutputs().put("commitHash", commitHash);
            return resp;
        }
    }
}
