package com.aiqaos.agent.impl;

import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.enums.AgentStatus;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestCaseGeneratorAgent implements Agent<AgentRequest, AgentResponse> {

    @Autowired
    private PromptEngine<PromptRequest, PromptResponse> promptEngine;

    @Autowired
    private LLMProviderManager providerManager;

    private AgentStatus status = AgentStatus.IDLE;

    @Override
    public AgentResponse execute(AgentRequest request, AgentContext context) {
        status = AgentStatus.EXECUTING;
        AgentResponse response = new AgentResponse();
        try {
            PromptRequest promptReq = new PromptRequest();
            promptReq.setTemplateName("TEST_CASE_GENERATION");
            
            Map<String, Object> params = new HashMap<>();
            params.put("analysis", request.getPrompt());
            promptReq.setParameters(params);
            
            PromptResponse promptRes = promptEngine.renderPrompt(promptReq);
            String renderedPrompt = promptRes.getRenderedContent();

            LLMRequest llmReq = new LLMRequest();
            llmReq.setPrompt(renderedPrompt);
            llmReq.setSystemPrompt(request.getSystemInstruction() != null ? request.getSystemInstruction() : "You are a Senior QA Test Designer Agent.");
            llmReq.setPurpose("TEST_CASE_GENERATION");
            llmReq.setAgentType("TEST_CASE_GENERATOR");
            if (request.getMetadata() != null && request.getMetadata().getCorrelationId() != null) {
                llmReq.setCorrelationId(request.getMetadata().getCorrelationId().toString());
            }

            LLMResponse llmRes = providerManager.generate(llmReq);

            response.setContent(llmRes.getText());
            response.setConfidenceScore(0.95);
            response.setStatus("SUCCESS");
            status = AgentStatus.IDLE;
        } catch (Exception e) {
            status = AgentStatus.ERROR;
            response.setStatus("FAILED");
            response.setMessage("Failed in TestCaseGeneratorAgent: " + e.getMessage());
        }
        return response;
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public AgentType getType() {
        return AgentType.TEST_CASE_GENERATOR;
    }
}
