package com.aiqaos.workflow.graph;

public class WorkflowEdge {
    private String fromNode;
    private String toNode;

    public WorkflowEdge() {}

    public WorkflowEdge(String fromNode, String toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public String getFromNode() { return fromNode; }
    public void setFromNode(String fromNode) { this.fromNode = fromNode; }
    public String getToNode() { return toNode; }
    public void setToNode(String toNode) { this.toNode = toNode; }
}