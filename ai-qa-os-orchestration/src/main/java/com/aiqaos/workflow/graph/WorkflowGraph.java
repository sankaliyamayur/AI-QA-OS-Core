package com.aiqaos.workflow.graph;

import java.util.ArrayList;
import java.util.List;

public class WorkflowGraph {
    private List<WorkflowNode> nodes = new ArrayList<>();
    private List<WorkflowEdge> edges = new ArrayList<>();

    public List<WorkflowNode> getNodes() { return nodes; }
    public void setNodes(List<WorkflowNode> nodes) { this.nodes = nodes; }
    public List<WorkflowEdge> getEdges() { return edges; }
    public void setEdges(List<WorkflowEdge> edges) { this.edges = edges; }
}