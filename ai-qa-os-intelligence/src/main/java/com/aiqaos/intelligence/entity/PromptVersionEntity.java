package com.aiqaos.intelligence.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prompt_versions")
public class PromptVersionEntity extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "version_val", nullable = false)
    private String versionTag;

    // V27: TEXT, not @Lob — see V26/agent_traces. DbPromptSource READS this column, so an oid
    // large object would fail with "Large Objects may not be used in auto-commit mode" on the
    // first stored version; it also leaks one object per row on delete.
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
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