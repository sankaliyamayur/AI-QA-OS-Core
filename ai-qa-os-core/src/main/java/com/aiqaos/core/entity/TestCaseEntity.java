package com.aiqaos.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "test_cases")
@Data
@EntityListeners(AuditingEntityListener.class)
public class TestCaseEntity {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @Column(name = "module_id", length = 50, nullable = false)
    private String moduleId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String feature;

    @Column(length = 50)
    private String priority;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String browser;

    @Column(length = 50)
    private String duration;

    @Column(length = 100)
    private String build;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Column(name = "last_run")
    private LocalDateTime lastRun;

    @Column(length = 100)
    private String device;

    @Column(length = 100)
    private String environment;

    @Column(name = "commit_hash", length = 100)
    private String commitHash;

    @Column(name = "pipeline_id", length = 100)
    private String pipelineId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "steps_json", columnDefinition = "TEXT")
    private String stepsJson;

    @Transient
    private List<Map<String, Object>> steps;

    @PostLoad
    private void postLoad() {
        if (stepsJson != null && !stepsJson.isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                steps = mapper.readValue(stepsJson, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                // Keep steps null if parsing fails
            }
        }
    }

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (steps != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                stepsJson = mapper.writeValueAsString(steps);
            } catch (Exception e) {
                // Ignore serialization error
            }
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getBuild() { return build; }
    public void setBuild(String build) { this.build = build; }
    public LocalDateTime getLastRun() { return lastRun; }
    public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public String getPipelineId() { return pipelineId; }
    public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }
    public List<Map<String, Object>> getSteps() { return steps; }

    /**
     * Serializes straight into the mapped column rather than waiting for @PrePersist.
     * Rows carry an assigned id, so save() routes through merge(), and merge does not
     * copy @Transient fields onto the managed instance — the callback would see null
     * and silently drop the steps.
     */
    public void setSteps(List<Map<String, Object>> steps) {
        this.steps = steps;
        if (steps != null) {
            try {
                this.stepsJson = new ObjectMapper().writeValueAsString(steps);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to serialize test case steps", e);
            }
        }
    }
}
