package com.aiqaos.workflow.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class WorkflowDslParser {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public WorkflowDefinition parse(String yamlContent) throws IOException {
        return mapper.readValue(yamlContent, WorkflowDefinition.class);
    }
}