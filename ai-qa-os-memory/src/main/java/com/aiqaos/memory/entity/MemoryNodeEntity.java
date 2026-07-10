package com.aiqaos.memory.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "memory_nodes")
public class MemoryNodeEntity extends BaseEntity {

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "memory_type", nullable = false)
    private String memoryType;

    @Column(name = "category")
    private String category;

    @Column(name = "owner_id")
    private String ownerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "memory_node_metadata", joinColumns = @JoinColumn(name = "memory_node_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata = new HashMap<>();

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMemoryType() { return memoryType; }
    public void setMemoryType(String memoryType) { this.memoryType = memoryType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}