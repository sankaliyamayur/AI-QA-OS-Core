package com.aiqaos.intelligence.manager;

import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.intelligence.component.PromptCompiler;
import com.aiqaos.intelligence.loader.PromptLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PromptManagerImpl implements PromptEngine<PromptRequest, PromptResponse> {

    @Autowired
    private PromptLoader promptLoader;

    @Autowired
    private PromptCompiler promptCompiler;

    @Autowired
    private PromptVersionManager versionManager;

    @Autowired
    private PromptCacheManager cacheManager;

    // FI-PE3-C: optional so the bean's absence (history disabled, or plain unit tests) changes nothing.
    @Autowired(required = false)
    private PromptExecutionRecorder executionRecorder;

    @Override
    public PromptResponse renderPrompt(PromptRequest request) {
        long startedAt = System.nanoTime();
        String templateName = request.getTemplateName();
        String activeVersion = versionManager.getActiveVersion(templateName);

        String cacheKey = templateName + ":" + activeVersion + ":" + request.getParameters().hashCode();
        Optional<String> cached = cacheManager.getCachedPrompt(cacheKey);

        String compiledText;
        if (cached.isPresent()) {
            compiledText = cached.get();
        } else {
            String templateText = promptLoader.loadTemplate(templateName, activeVersion);
            // Compile parameters into map matching Pebble bindings
            Map<String, Object> params = new HashMap<>();
            request.getParameters().forEach(params::put);

            compiledText = promptCompiler.compile(templateText, params);
            cacheManager.cachePrompt(cacheKey, compiledText);
        }

        // FI-PE3-C: record the render that actually happened (opt-in; best-effort, never fails a render).
        if (executionRecorder != null) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            executionRecorder.record(templateName, activeVersion, compiledText, elapsedMs);
        }

        PromptResponse response = new PromptResponse();
        response.getMetadata().setCorrelationId(request.getMetadata().getCorrelationId());
        response.getMetadata().setTraceId(request.getMetadata().getTraceId());
        response.setRenderedContent(compiledText);
        response.setStatus("SUCCESS");
        return response;
    }
}