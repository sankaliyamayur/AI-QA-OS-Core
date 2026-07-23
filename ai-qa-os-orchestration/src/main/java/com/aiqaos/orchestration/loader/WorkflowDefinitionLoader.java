package com.aiqaos.orchestration.loader;

import com.aiqaos.orchestration.dsl.WorkflowDefinition;
import com.aiqaos.orchestration.dsl.WorkflowDslParser;
import com.aiqaos.orchestration.registry.WorkflowRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class WorkflowDefinitionLoader {

    @Autowired
    private WorkflowDslParser parser;

    @Autowired
    private WorkflowRegistry registry;

    public void loadFromYaml(String yamlContent) throws IOException {
        WorkflowDefinition def = parser.parse(yamlContent);
        registry.register(def);
    }
}