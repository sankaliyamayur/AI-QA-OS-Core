package com.aiqaos.observability.logging;

import org.springframework.stereotype.Component;

@Component
public class ExceptionAnalyzer {

    public String analyze(Throwable err) {
        if (err == null) return "UNKNOWN";
        return err.getClass().getSimpleName();
    }
}