package com.aiqaos.memory.component;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemoryCompressor {
    public String compress(List<String> historyLines) {
        if (historyLines == null || historyLines.isEmpty()) {
            return "";
        }
        return historyLines.stream()
                .limit(10)
                .collect(Collectors.joining("\nSummary: "));
    }
}