package com.aiqaos.core.guardrail;

/**
 * Where a {@link Guardrail} check is happening: on the way into the AI ({@link Phase#INPUT})
 * or on the way out ({@link Phase#OUTPUT}), and which step/agent produced the content.
 */
public class GuardrailContext {

    public enum Phase {
        INPUT,
        OUTPUT
    }

    private final Phase phase;
    private final String source;

    public GuardrailContext(Phase phase, String source) {
        this.phase = phase;
        this.source = source;
    }

    public static GuardrailContext input(String source) {
        return new GuardrailContext(Phase.INPUT, source);
    }

    public static GuardrailContext output(String source) {
        return new GuardrailContext(Phase.OUTPUT, source);
    }

    public Phase getPhase() {
        return phase;
    }

    public String getSource() {
        return source;
    }
}
