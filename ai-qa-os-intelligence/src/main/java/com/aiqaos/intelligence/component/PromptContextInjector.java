package com.aiqaos.intelligence.component;

import com.aiqaos.core.context.AgentContext;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PromptContextInjector {
    public Map<String, Object> inject(AgentContext context, Map<String, Object> params) {
        Map<String, Object> consolidated = new HashMap<>();
        if (params != null) {
            consolidated.putAll(params);
        }
        if (context != null) {
            consolidated.putAll(context.getPromptContext());
            consolidated.putAll(context.getUserContext());
            consolidated.putAll(context.getEnvironmentContext());
        }
        return consolidated;
    }
}