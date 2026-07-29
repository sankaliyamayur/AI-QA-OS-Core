package com.aiqaos.agent.roster;

/**
 * AGT-1: a roster agent's build status — {@code IMPLEMENTED} (a live agent exists), {@code DESIGNED}
 * (specified in a roadmap item, not yet a standalone agent), or {@code FUTURE} (planned specialist,
 * built incrementally via DX-2/PLG-3).
 */
public enum AgentStatus {
    IMPLEMENTED,
    DESIGNED,
    FUTURE
}
