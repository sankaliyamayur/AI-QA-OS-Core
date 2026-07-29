package com.aiqaos.notification.event;

/**
 * ENT-2: the platform events that warrant a stakeholder notification — a run finished, a run failed,
 * or an action needs human approval (the AI-2 hook).
 */
public enum NotificationEventType {
    RUN_COMPLETE,
    RUN_FAILURE,
    APPROVAL_REQUEST
}
