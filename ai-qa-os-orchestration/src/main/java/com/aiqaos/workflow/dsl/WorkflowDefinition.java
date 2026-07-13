package com.aiqaos.workflow.dsl;

import com.aiqaos.workflow.graph.WorkflowEdge;
import com.aiqaos.workflow.graph.WorkflowNode;
import java.util.ArrayList;
import java.util.List;

public class WorkflowDefinition {
    private String id;
    private String name;
    private String description;
    private String version;
    private List<WorkflowNode> steps = new ArrayList<>();
    private List<WorkflowEdge> edges = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public List<WorkflowNode> getSteps() { return steps; }
    public void setSteps(List<WorkflowNode> steps) { this.steps = steps; }
    public List<WorkflowEdge> getEdges() { return edges; }
    public void setEdges(List<WorkflowEdge> edges) { this.edges = edges; }
}