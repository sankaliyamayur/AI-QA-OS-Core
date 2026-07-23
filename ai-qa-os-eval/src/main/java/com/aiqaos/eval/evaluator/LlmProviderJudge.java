package com.aiqaos.eval.evaluator;

import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import org.springframework.stereotype.Component;

/**
 * Production {@link LlmJudgeEvaluator.JudgeLlm}: routes the judge prompt through the
 * platform's {@code LLMProviderManager} at temperature 0 for stable scoring.
 */
@Component
public class LlmProviderJudge implements LlmJudgeEvaluator.JudgeLlm {

    private final LLMProviderManager providerManager;

    public LlmProviderJudge(LLMProviderManager providerManager) {
        this.providerManager = providerManager;
    }

    @Override
    public String judge(String systemPrompt, String userPrompt) {
        LLMRequest request = new LLMRequest();
        request.setSystemPrompt(systemPrompt);
        request.setPrompt(userPrompt);
        request.setTemperature(0.0);
        request.setPurpose("eval-llm-judge");
        return providerManager.generate(request).getText();
    }
}
