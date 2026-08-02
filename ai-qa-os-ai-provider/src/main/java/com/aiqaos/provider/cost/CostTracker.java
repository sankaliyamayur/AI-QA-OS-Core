package com.aiqaos.provider.cost;

import com.aiqaos.observability.entity.AgentTraceEntity;
import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.AgentTraceRepository;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CostTracker {

    private final LLMCostRepository costRepository;
    private final AgentTraceRepository agentTraceRepository;

    // ENT-3: optional — records spend into the in-memory quota ledger (null in direct-construction tests).
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SpendLedger spendLedger;

    // AI-6: optional — records actual token usage into the in-memory token/context-budget ledger.
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private TokenLedger tokenLedger;

    public CostTracker(LLMCostRepository costRepository, AgentTraceRepository agentTraceRepository) {
        this.costRepository = costRepository;
        this.agentTraceRepository = agentTraceRepository;
    }

    public void track(LLMRequest req, LLMResponse resp, String providerName) {
        double cost = calculateCost(providerName, resp.getModel(), resp.getUsage().getInputTokens(), resp.getUsage().getOutputTokens());

        LLMCostEntity entity = new LLMCostEntity();
        entity.setRequestId(req.getCorrelationId());
        entity.setAgentType(req.getAgentType());
        entity.setPurpose(req.getPurpose());
        entity.setLatencyMs(resp.getLatencyMs());
        entity.setProvider(providerName);
        entity.setModel(resp.getModel());
        entity.setInputTokens(resp.getUsage().getInputTokens());
        entity.setOutputTokens(resp.getUsage().getOutputTokens());
        entity.setCost(cost);
        entity.setTimestamp(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setDeleted(false);

        costRepository.save(entity);

        // Enterprise-grade dashboard drill-down: durable prompt/response history per LLM call.
        AgentTraceEntity trace = new AgentTraceEntity();
        trace.setCorrelationId(req.getCorrelationId());
        trace.setAgentType(req.getAgentType());
        trace.setPurpose(req.getPurpose());
        trace.setProvider(providerName);
        trace.setModel(resp.getModel());
        trace.setPrompt(req.getPrompt());
        trace.setResponse(resp.getText());
        trace.setPromptTokens(resp.getUsage().getInputTokens());
        trace.setCompletionTokens(resp.getUsage().getOutputTokens());
        trace.setLatencyMs(resp.getLatencyMs());
        trace.setTimestamp(LocalDateTime.now());
        agentTraceRepository.save(trace);

        // ENT-3: feed the fast quota ledger with the actual cost (durable record is the row above).
        if (spendLedger != null) {
            spendLedger.record(cost, req.getCorrelationId(), req.getAgentType());
        }

        // AI-6: feed the token/context-budget ledger with the actual token usage (input + output).
        if (tokenLedger != null) {
            tokenLedger.record(
                    resp.getUsage().getInputTokens() + resp.getUsage().getOutputTokens(),
                    req.getCorrelationId(), req.getAgentType());
        }
    }

    private double calculateCost(String provider, String model, long input, long output) {
        // Dummy standard calculations. Rates per 1M tokens
        double inputRate = 5.0;
        double outputRate = 15.0;

        if (model.contains("mini")) {
            inputRate = 0.15;
            outputRate = 0.60;
        } else if (model.contains("flash")) {
            inputRate = 0.35;
            outputRate = 1.05;
        }

        return ((input * inputRate) + (output * outputRate)) / 1000000.0;
    }
}