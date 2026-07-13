package com.aiqaos.workflow.context;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkflowVariables implements Serializable {
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    public void set(String key, Object value) {
        if (key != null && value != null) {
            variables.put(key, value);
        }
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public Map<String, Object> getMap() {
        return variables;
    }
}