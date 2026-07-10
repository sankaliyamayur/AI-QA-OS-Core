package com.aiqaos.core.model;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeNode {
    private String nodeId;
    private String category;
    private String content;
    private List<String> relatedNodeIds;
}
