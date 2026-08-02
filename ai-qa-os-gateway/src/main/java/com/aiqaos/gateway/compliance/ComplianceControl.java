package com.aiqaos.gateway.compliance;

/**
 * GOV-2 (ADR-081): one compliance control — a framework requirement mapped to the platform capability
 * that satisfies it, with an honest status and an evidence pointer. This is <b>self-attestation</b>
 * (a factual statement of an implemented capability), not a formal certification.
 *
 * @param id             stable control id
 * @param framework      the compliance framework
 * @param requirementRef the framework's requirement reference (e.g. "CC6.1", "A.9.4", "Art.32")
 * @param title          what the requirement covers
 * @param satisfiedBy    the platform capability that satisfies it (blank when NOT_IMPLEMENTED)
 * @param status         SATISFIED / PARTIAL / NOT_IMPLEMENTED
 * @param evidence       a short note pointing at the implementing item/ADR
 */
public record ComplianceControl(
        String id,
        ComplianceFramework framework,
        String requirementRef,
        String title,
        String satisfiedBy,
        ControlStatus status,
        String evidence) {
}
