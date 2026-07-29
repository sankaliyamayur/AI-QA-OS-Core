package com.aiqaos.healing.locator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** HEAL-1: unit tests for deterministic locator candidate generation + ranking. No Mockito. */
class HeuristicLocatorHealerTest {

    private final HeuristicLocatorHealer healer = new HeuristicLocatorHealer();

    @Test
    void prefersTestIdWhenPresent() {
        List<LocatorCandidate> c = healer.propose(
                LocatorHealingRequest.of("#old", Map.of("data-testid", "submit-btn", "id", "old")));
        assertThat(c.get(0).getStrategy()).isEqualTo(LocatorStrategy.TEST_ID);
        assertThat(c.get(0).getValue()).contains("submit-btn");
        assertThat(c.get(0).getConfidence()).isEqualTo(0.95);
    }

    @Test
    void fallsBackToIdWhenNoTestId() {
        List<LocatorCandidate> c = healer.propose(
                LocatorHealingRequest.of("//x", Map.of("id", "loginButton")));
        assertThat(c.get(0).getStrategy()).isEqualTo(LocatorStrategy.ID);
        assertThat(c.get(0).getValue()).isEqualTo("#loginButton");
    }

    @Test
    void ranksMoreRobustStrategiesFirst() {
        List<LocatorCandidate> c = healer.propose(LocatorHealingRequest.of("//x",
                Map.of("data-testid", "t", "id", "i", "class", "btn primary")));
        // Expected order by robustness: TEST_ID > ID > CSS (> relaxed XPATH fallback).
        assertThat(c).extracting(LocatorCandidate::getStrategy)
                .containsSubsequence(LocatorStrategy.TEST_ID, LocatorStrategy.ID, LocatorStrategy.CSS);
    }

    @Test
    void buildsCssSelectorFromClassList() {
        List<LocatorCandidate> c = healer.propose(
                LocatorHealingRequest.of(null, Map.of("class", "btn primary")));
        assertThat(c).anySatisfy(cand -> {
            assertThat(cand.getStrategy()).isEqualTo(LocatorStrategy.CSS);
            assertThat(cand.getValue()).isEqualTo(".btn.primary");
        });
    }

    @Test
    void sparseInputStillYieldsARelaxedXpathFallback() {
        List<LocatorCandidate> c = healer.propose(
                LocatorHealingRequest.of("//div[2]/button[3]", Map.of()));
        assertThat(c).isNotEmpty();
        LocatorCandidate last = c.get(c.size() - 1);
        assertThat(last.getStrategy()).isEqualTo(LocatorStrategy.XPATH);
        assertThat(last.getValue()).isEqualTo("//div/button"); // positional indexes stripped
    }

    @Test
    void nothingKnownYieldsNoCandidates() {
        assertThat(healer.propose(LocatorHealingRequest.of(null, Map.of()))).isEmpty();
        assertThat(healer.propose(null)).isEmpty();
    }
}
