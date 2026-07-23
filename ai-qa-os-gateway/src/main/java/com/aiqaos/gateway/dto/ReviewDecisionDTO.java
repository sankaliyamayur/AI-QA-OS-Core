package com.aiqaos.gateway.dto;

/** AI-2 — request body for approve/reject: who decided and an optional comment. */
public class ReviewDecisionDTO {
    private String reviewer;
    private String comment;

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
