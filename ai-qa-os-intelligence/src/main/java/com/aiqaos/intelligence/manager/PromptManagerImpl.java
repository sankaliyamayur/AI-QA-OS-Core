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

    @Override
    public PromptResponse renderPrompt(PromptRequest request) {
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

        PromptResponse response = new PromptResponse();
        response.getMetadata().setCorrelationId(request.getMetadata().getCorrelationId());
        response.getMetadata().setTraceId(request.getMetadata().getTraceId());
        response.setRenderedContent(compiledText);
        response.setStatus("SUCCESS");
        return response;
    }
}