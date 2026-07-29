package com.aiqaos.eval.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * PE-2: ranks prompt versions by their measured {@link PromptScore} (from PE-1's
 * {@link PromptBenchmarkService}) over a set of golden suites — best-measured-prompt first, so the
 * winner is decided on evidence, not opinion. Reuses PE-1's scoring; adds no new evaluation.
 */
@Component
public class PromptLeaderboard {

    private final PromptBenchmarkService benchmarkService;

    public PromptLeaderboard(PromptBenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    /** Rank {@code versionIds} by their overall benchmark score across {@code suites} (descending). */
    public List<LeaderboardEntry> rank(List<String> versionIds, List<String> suites) {
        List<LeaderboardEntry> scored = new ArrayList<>();
        for (String versionId : versionIds) {
            double overall = benchmarkService.benchmark(versionId, suites).getOverall();
            scored.add(new LeaderboardEntry(versionId, overall, 0));
        }
        scored.sort(Comparator.comparingDouble(LeaderboardEntry::getScore).reversed());

        List<LeaderboardEntry> ranked = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            LeaderboardEntry e = scored.get(i);
            ranked.add(new LeaderboardEntry(e.getVersionId(), e.getScore(), i + 1));
        }
        return ranked;
    }
}
