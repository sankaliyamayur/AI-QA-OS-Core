package com.aiqaos.intelligence.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptCompiler {

    @Autowired
    private PromptTemplateEngine templateEngine;

    @Autowired
    private PromptValidator validator;

    @Autowired
    private PromptSecurityGuard securityGuard;

    public String compile(String templateText, Map<String, Object> params) {
        validator.validateParams(templateText, params);
        String compiled = templateEngine.render(templateText, params);
        securityGuard.scan(compiled);
        return compiled;
    }
}