package com.aiqaos.eval.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** PE-2: the leaderboard ranks versions by their PE-1 benchmark score, best first. */
class PromptLeaderboardTest {

    /** Stub PE-1 benchmark: canned overall score per versionId (harness unused — overridden). */
    private static PromptBenchmarkService benchmarkReturning(Map<String, Double> scores) {
        return new PromptBenchmarkService(null) {
            @Override
            public PromptScore benchmark(String promptRef, List<String> suites) {
                return new PromptScore(promptRef, scores.getOrDefault(promptRef, 0.0), Map.of(), 0);
            }
        };
    }

    @Test
    void ranksVersionsByScoreDescendingWithRanks() {
        PromptLeaderboard board = new PromptLeaderboard(
                benchmarkReturning(Map.of("v-lo", 0.5, "v-hi", 0.9, "v-mid", 0.7)));

        List<LeaderboardEntry> ranked = board.rank(List.of("v-lo", "v-hi", "v-mid"), List.of("sample-suite"));

        assertThat(ranked).extracting(LeaderboardEntry::getVersionId)
                .containsExactly("v-hi", "v-mid", "v-lo");
        assertThat(ranked).extracting(LeaderboardEntry::getRank)
                .containsExactly(1, 2, 3);
        assertThat(ranked.get(0).getScore()).isEqualTo(0.9);
    }

    @Test
    void emptyVersionListYieldsEmptyLeaderboard() {
        PromptLeaderboard board = new PromptLeaderboard(benchmarkReturning(Map.of()));
        assertThat(board.rank(List.of(), List.of("sample-suite"))).isEmpty();
    }
}
