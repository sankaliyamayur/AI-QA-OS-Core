package com.aiqaos.eval.harness;

import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Reference {@link PromptRunner}: renders the named prompt version through the core
 * {@link PromptEngine} contract when {@code intelligence} is wired (optional, via
 * {@link ObjectProvider}), otherwise uses the case input directly; then executes it through
 * {@link LLMProviderManager}. This is what "consumes {@code intelligence} prompt versions" means
 * concretely, without hard-coupling the eval module to the intelligence API.
 */
@Component
public class LlmPromptRunner implements PromptRunner {

    private final LLMProviderManager providerManager;
    private final ObjectProvider<PromptEngine<PromptRequest, PromptResponse>> promptEngineProvider;

    public LlmPromptRunner(LLMProviderManager providerManager,
                           ObjectProvider<PromptEngine<PromptRequest, PromptResponse>> promptEngineProvider) {
        this.providerManager = providerManager;
        this.promptEngineProvider = promptEngineProvider;
    }

    @Override
    public String run(String promptRef, EvaluationCase testCase) {
        String prompt = renderOrFallback(promptRef, testCase);
        LLMRequest request = new LLMRequest();
        request.setPrompt(prompt);
        request.setPurpose("eval-harness");
        return providerManager.generate(request).getText();
    }

    private String renderOrFallback(String promptRef, EvaluationCase testCase) {
        PromptEngine<PromptRequest, PromptResponse> engine = promptEngineProvider.getIfAvailable();
        if (engine != null && promptRef != null && !promptRef.isBlank()) {
            try {
                PromptRequest request = new PromptRequest();
                request.setTemplateName(promptRef);
                Map<String, Object> params = new HashMap<>();
                params.put("input", testCase.getInput());
                request.setParameters(params);
                PromptResponse response = engine.renderPrompt(request);
                if (response != null && response.getRenderedContent() != null
                        && !response.getRenderedContent().isBlank()) {
                    return response.getRenderedContent();
                }
            } catch (Exception ignored) {
                // fall back to the raw case input below
            }
        }
        return testCase.getInput() == null ? "" : testCase.getInput();
    }
}
