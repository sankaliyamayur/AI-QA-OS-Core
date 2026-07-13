package com.aiqaos.memory.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryMetadata {
    private UUID id;
    private UUID tenantId;
    private String project;
    private String module;
    private String documentType;
    private String source;
    private String author;
    private String version;
    private boolean isLatest = true;
    private UUID parentVersion;
    private String language;
    private LocalDateTime indexedAt = LocalDateTime.now();
    private Map<String, Object> attributes = new HashMap<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public boolean isLatest() { return isLatest; }
    public void setLatest(boolean latest) { isLatest = latest; }
    public UUID getParentVersion() { return parentVersion; }
    public void setParentVersion(UUID parentVersion) { this.parentVersion = parentVersion; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public LocalDateTime getIndexedAt() { return indexedAt; }
    public void setIndexedAt(LocalDateTime indexedAt) { this.indexedAt = indexedAt; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}