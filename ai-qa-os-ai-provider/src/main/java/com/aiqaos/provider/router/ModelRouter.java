package com.aiqaos.provider.router;

import org.springframework.stereotype.Component;

@Component
public class ModelRouter {

    public String routeModel(String purpose) {
        if (purpose == null) return "Gemini";
        
        switch (purpose.toLowerCase()) {
            case "requirement-analysis" -> { return "Claude"; }
            case "code-generation"       -> { return "OpenAI"; }
            case "bug-analysis"          -> { return "Claude"; }
            default                      -> { return "Gemini"; }
        }
    }
}