package com.aiqaos.intelligence.component;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptTemplateEngine {
    public String render(String template, Map<String, Object> params) {
        if (template == null) return "";
        if (params == null) return template;
        
        String rendered = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());
            rendered = rendered.replace("{{" + key + "}}", value);
        }
        return rendered;
    }
}