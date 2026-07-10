package com.aiqaos.memory.component;

import com.aiqaos.memory.model.MemoryNodeDTO;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemoryContextBuilder {
    public String buildFormattedContext(List<MemoryNodeDTO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return "No contextual memory nodes retrieved.";
        }
        return nodes.stream()
                .map(node -> String.format("[Memory Node - Type: %s, Category: %s]\nContent: %s", 
                        node.getMemoryType(), node.getCategory(), node.getContent()))
                .collect(Collectors.joining("\n\n"));
    }
}