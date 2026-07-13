package com.aiqaos.intelligence.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prompt_versions")
public class PromptVersionEntity extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "version_val", nullable = false)
    private String versionTag;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "author")
    private String author;

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public String getVersionTag() { return versionTag; }
    public void setVersionTag(String versionTag) { this.versionTag = versionTag; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}