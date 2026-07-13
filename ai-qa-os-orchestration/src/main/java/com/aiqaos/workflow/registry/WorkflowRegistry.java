package com.aiqaos.workflow.registry;

import com.aiqaos.workflow.dsl.WorkflowDefinition;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowRegistry {
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();

    public void register(WorkflowDefinition definition) {
        definitions.put(definition.getId(), definition);
    }

    public WorkflowDefinition getWorkflow(String id) {
        return definitions.get(id);
    }
}