package com.aiqaos.agent.component;

import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.core.engine.MemoryEngine;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.contract.MemoryRequest;
import com.aiqaos.core.contract.MemoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
public class AgentExecutor {

    @Autowired(required = false)
    private PromptEngine<PromptRequest, PromptResponse> promptEngine;

    @Autowired(required = false)
    private MemoryEngine<MemoryRequest, MemoryResponse> memoryEngine;

    public AgentResponse executeAgent(AgentRequest request) {
        String compiledPrompt = "Mock compiled prompt context";
        if (promptEngine != null) {
            PromptRequest pReq = new PromptRequest();
            pReq.setTemplateName("CODE_GENERATOR_MASTER_PROMPT");
            pReq.setParameters(new HashMap<>());
            pReq.getParameters().put("task", request.getPrompt());
            try {
                PromptResponse pRes = promptEngine.renderPrompt(pReq);
                compiledPrompt = pRes.getRenderedContent();
            } catch (Exception e) {
                // Fallback
            }
        }

        if (memoryEngine != null && request.getPrompt() != null) {
            MemoryRequest mReq = new MemoryRequest();
            mReq.setQuery(request.getPrompt());
            mReq.setMaxElements(5);
            try {
                memoryEngine.queryMemory(mReq);
            } catch (Exception e) {
                // Fallback
            }
        }

        AgentResponse response = new AgentResponse();
        response.getMetadata().setCorrelationId(request.getMetadata().getCorrelationId());
        response.getMetadata().setTraceId(request.getMetadata().getTraceId());
        response.setContent("Processed request for task: " + request.getPrompt() + " using prompt: " + compiledPrompt);
        response.setConfidenceScore(0.95);
        response.setStatus("SUCCESS");
        return response;
    }
}