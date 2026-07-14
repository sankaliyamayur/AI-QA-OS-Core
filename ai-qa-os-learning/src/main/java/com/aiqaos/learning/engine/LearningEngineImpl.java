package com.aiqaos.learning.engine;

import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.LearningEvent;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.SelfHealingRecommendation;
import com.aiqaos.learning.analyzer.FailurePatternAnalyzer;
import com.aiqaos.learning.healing.SelfHealingEngine;
import com.aiqaos.learning.memory.LearningMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@SuppressWarnings("unchecked")
public class LearningEngineImpl implements LearningEngine {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private FailurePatternAnalyzer analyzer;

    @Autowired
    private SelfHealingEngine healingEngine;

    @Autowired
    private LearningMemoryStore memoryStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AgentResponse analyze(QAExecutionReport report, WorkflowContext context) {
        try {
            // Retrieve Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>)
                    agentManager.getAgentByType(AgentType.LEARNING_ENGINEER);

            if (agent == null) {
                throw new IllegalStateException("Agent of type LEARNING_ENGINEER is not registered in the system");
            }

            // Retrieve previous history from memory
            List<FailurePattern> history = memoryStore.getFailurePatterns();

            // Structure prompt payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("execution", report.getExecutionResult());
            payload.put("bugs", report.getBugAnalysisReport());
            payload.put("scripts", report.getScriptSuite());
            payload.put("history", history);

            String requestJson;
            try {
                requestJson = objectMapper.writeValueAsString(payload);
            } catch (Exception serEx) {
                requestJson = "{\"error\":\"Serialization failed\",\"message\":\"" + serEx.getMessage() + "\"}";
            }

            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(requestJson);
            UUID corrId = context.getMetadata() != null ? context.getMetadata().getCorrelationId() : null;
            if (corrId != null) {
                agentReq.getMetadata().setCorrelationId(corrId);
            }

            // Execute Agent
            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Learning Agent failed: " + agentRes.getMessage());
            }

            return agentRes;

        } catch (Exception e) {
            // Internal heuristic fallback packaged as AgentResponse
            AgentResponse fallbackResponse = new AgentResponse();
            fallbackResponse.setStatus("SUCCESS");
            try {
                LearningResult fallbackResult = runLocalHeuristicAnalysis(report);
                fallbackResponse.setContent(objectMapper.writeValueAsString(fallbackResult));
            } catch (Exception ex) {
                fallbackResponse.setStatus("FAILED");
                fallbackResponse.setMessage("Heuristic fallback failed: " + ex.getMessage());
            }
            return fallbackResponse;
        }
    }

    private LearningResult runLocalHeuristicAnalysis(QAExecutionReport report) {
        LearningResult fallback = new LearningResult();

        // Use components locally
        List<FailurePattern> history = memoryStore.getFailurePatterns();
        List<FailurePattern> patterns = analyzer.analyzeFailures(report, history);
        List<SelfHealingRecommendation> recommendations = healingEngine.generateRecommendations(report);

        fallback.setPatterns(patterns);
        fallback.setRecommendations(recommendations);

        // Generate a fallback event
        LearningEvent event = new LearningEvent();
        event.setEventId("EVT-FALLBACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        event.setExecutionId(report.getExecutionId() != null ? report.getExecutionId() : "EXEC-UNKNOWN");
        event.setEventType("FAILURE_PATTERN");
        event.setCategory("HEURISTIC");
        event.setDescription("Fallback heuristic learning performed due to agent failure");
        event.setSourceAgent("AI-QA-OS LearningEngine (local)");
        event.setCreatedTime(LocalDateTime.now());
        fallback.getEvents().add(event);

        return fallback;
    }
}
