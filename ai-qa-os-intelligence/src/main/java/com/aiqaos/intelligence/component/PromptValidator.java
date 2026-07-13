package com.aiqaos.intelligence.component;

import org.springframework.stereotype.Component;
import com.aiqaos.core.exception.ValidationException;

@Component
public class PromptValidator {
    public void validateParams(String template, java.util.Map<String, Object> params) {
        if (template == null) return;
        // Check for missing bindings in Pebble-style syntax
        int idx = 0;
        while ((idx = template.indexOf("{{", idx)) != -1) {
            int end = template.indexOf("}}", idx);
            if (end == -1) break;
            String placeholder = template.substring(idx + 2, end).trim();
            if (params == null || !params.containsKey(placeholder)) {
                throw new ValidationException("Missing prompt rendering parameter mapping: " + placeholder);
            }
            idx = end + 2;
        }
    }
}