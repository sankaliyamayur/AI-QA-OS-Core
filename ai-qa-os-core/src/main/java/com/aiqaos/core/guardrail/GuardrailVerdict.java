package com.aiqaos.core.guardrail;

/**
 * The result of a {@link Guardrail} check: whether the content is allowed, what action to
 * take, and (for {@link Action#SANITIZE}) the cleaned content.
 */
public class GuardrailVerdict {

    public enum Action {
        ALLOW,
        BLOCK,
        SANITIZE
    }

    private final boolean allowed;
    private final Action action;
    private final String reason;
    private final String sanitizedContent;

    private GuardrailVerdict(boolean allowed, Action action, String reason, String sanitizedContent) {
        this.allowed = allowed;
        this.action = action;
        this.reason = reason;
        this.sanitizedContent = sanitizedContent;
    }

    public static GuardrailVerdict allow() {
        return new GuardrailVerdict(true, Action.ALLOW, "allowed", null);
    }

    public static GuardrailVerdict block(String reason) {
        return new GuardrailVerdict(false, Action.BLOCK, reason, null);
    }

    public static GuardrailVerdict sanitize(String sanitizedContent, String reason) {
        return new GuardrailVerdict(true, Action.SANITIZE, reason, sanitizedContent);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public Action getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public String getSanitizedContent() {
        return sanitizedContent;
    }
}
