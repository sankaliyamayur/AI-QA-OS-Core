package com.aiqaos.healing.approval;

/**
 * HEAL-2: the state of a heal in the approval workflow. High-confidence heals are
 * {@code AUTO_APPROVED}; softer ones become {@code PENDING_APPROVAL} until a human moves them to
 * {@code APPROVED} or {@code REJECTED}; unreported-confidence heals are {@code REJECTED} outright.
 */
public enum HealingApprovalStatus {
    AUTO_APPROVED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}
