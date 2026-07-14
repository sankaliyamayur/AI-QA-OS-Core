package com.aiqaos.healing.service;

import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.QAExecutionReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@SuppressWarnings("unchecked")
public class ScriptGenerationServiceImpl implements ScriptGenerationService {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public GeneratedScriptSuite regenerate(QAExecutionReport report, String reason) {
        try {
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>)
                    agentManager.getAgentByType(AgentType.SCRIPT_GENERATOR);

            if (agent == null) {
                throw new IllegalStateException("Agent SCRIPT_GENERATOR is not registered");
            }

            AgentRequest agentReq = new AgentRequest();
            // Packaging test suite context and healing reason in prompt request
            agentReq.setPrompt(objectMapper.writeValueAsString(report.getTestSuite()) 
                    + "\nHealing context: " + reason);
            agentReq.getMetadata().setCorrelationId(UUID.randomUUID());

            AgentResponse response = agent.execute(agentReq, new AgentContext());
            if ("SUCCESS".equals(response.getStatus()) && response.getContent() != null) {
                // Since this runs inside healing module which does not import LLMResponseValidator to avoid circular loop, 
                // we'll parse it simply using objectMapper directly. The LLM output is expected to be corrected by SCRIPT_GENERATOR configuration.
                return objectMapper.readValue(response.getContent(), GeneratedScriptSuite.class);
            }
        } catch (Exception e) {
            // Ignore and fall back to original scripts
        }
        return report.getScriptSuite();
    }
}
