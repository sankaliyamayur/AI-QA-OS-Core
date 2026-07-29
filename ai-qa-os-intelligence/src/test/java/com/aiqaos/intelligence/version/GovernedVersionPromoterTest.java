package com.aiqaos.intelligence.version;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.intelligence.governance.policy.ResponsibleAiPolicyProperties;
import com.aiqaos.intelligence.governance.policy.RuleBasedResponsibleAiPolicyEngine;
import org.junit.jupiter.api.Test;

/**
 * GOV-3 × GOV-4: unit tests for policy-gated version promotion — clean content is pinned, policy-
 * violating content is blocked (never pinned). Uses the real registry + policy engine. No Mockito.
 */
class GovernedVersionPromoterTest {

    private GovernedVersionPromoter promoter() {
        VersionRegistry registry = new VersionRegistry(new InMemoryVersionPinStore());
        RuleBasedResponsibleAiPolicyEngine policy =
                new RuleBasedResponsibleAiPolicyEngine(new ResponsibleAiPolicyProperties());
        return new GovernedVersionPromoter(registry, policy);
    }

    @Test
    void cleanContentIsPromotedAndPinned() {
        GovernedVersionPromoter p = promoter();
        VersionPromotionDecision d = p.promote("prompt:greeting", VersionKind.PROMPT, "v2", "alice",
                "Welcome to the app. Please sign in to continue.");

        assertThat(d.isPromoted()).isTrue();
        assertThat(d.getPin()).isNotNull();
        assertThat(d.getPin().getVersionTag()).isEqualTo("v2");
    }

    @Test
    void piiContentIsBlockedFromPromotion() {
        GovernedVersionPromoter p = promoter();
        VersionPromotionDecision d = p.promote("prompt:greeting", VersionKind.PROMPT, "v3", "bob",
                "Contact the admin at alice@example.com for access.");

        assertThat(d.isPromoted()).isFalse();
        assertThat(d.getStatus()).isEqualTo(VersionPromotionDecision.PromotionStatus.BLOCKED);
        assertThat(d.getReason()).contains("no-pii-in-prompts");
        assertThat(d.getPin()).isNull();
    }

    @Test
    void destructiveContentIsBlockedFromPromotion() {
        VersionPromotionDecision d = promoter().promote("prompt:cleanup", VersionKind.PROMPT, "v1",
                "carol", "First run DROP TABLE users to reset the environment.");
        assertThat(d.isPromoted()).isFalse();
        assertThat(d.getReason()).contains("destructive-requires-review");
    }

    @Test
    void blockedPromotionDoesNotChangeTheActiveVersion() {
        VersionRegistry registry = new VersionRegistry(new InMemoryVersionPinStore());
        RuleBasedResponsibleAiPolicyEngine policy =
                new RuleBasedResponsibleAiPolicyEngine(new ResponsibleAiPolicyProperties());
        GovernedVersionPromoter p = new GovernedVersionPromoter(registry, policy);

        p.promote("prompt:x", VersionKind.PROMPT, "v1", "alice", "clean prompt");      // pinned
        p.promote("prompt:x", VersionKind.PROMPT, "v2", "bob", "leak alice@example.com"); // blocked

        assertThat(registry.activeVersion("prompt:x")).contains("v1"); // unchanged by the blocked one
    }
}
