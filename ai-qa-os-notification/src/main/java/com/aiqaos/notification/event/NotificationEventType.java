package com.aiqaos.notification.event;

/**
 * ENT-2: the platform events that warrant a stakeholder notification — a run finished, a run failed,
 * an action needs human approval (the AI-2 hook), or prompt quality has regressed (the FI-PE3-D hook).
 */
public enum NotificationEventType {
    RUN_COMPLETE,
    RUN_FAILURE,
    APPROVAL_REQUEST,

    /**
     * FI-PE3-D: a prompt version's recent evaluation scores have declined materially below its earlier
     * scores (PE-3 / FI-PE3-B detection). Distinct from {@link #RUN_FAILURE} — nothing failed; the
     * platform is getting quietly worse, which is precisely why it needs to be pushed rather than
     * waited for on a dashboard.
     */
    PROMPT_REGRESSION
}
