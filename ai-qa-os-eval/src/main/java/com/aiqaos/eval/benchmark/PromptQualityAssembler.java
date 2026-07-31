package com.aiqaos.eval.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * PE-3: composes PE-2's {@link LeaderboardEntry} rows into a {@link PromptQualitySummary} for the
 * prompt-quality dashboard — best/worst version, average score, spread, and the ranked standings.
 * Pure — no I/O — so it is trivially unit-testable (the HEAL-3/LRN-3 assembler pattern).
 */
@Component
public class PromptQualityAssembler {

    public PromptQualitySummary summarize(List<LeaderboardEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return PromptQualitySummary.empty();
        }

        LeaderboardEntry best = entries.stream()
                .max(Comparator.comparingDouble(LeaderboardEntry::getScore)).orElseThrow();
        LeaderboardEntry worst = entries.stream()
                .min(Comparator.comparingDouble(LeaderboardEntry::getScore)).orElseThrow();
        double average = entries.stream().mapToDouble(LeaderboardEntry::getScore).average().orElse(0.0);

        // Standings ordered by rank (1 = best).
        List<LeaderboardEntry> standings = new ArrayList<>(entries);
        standings.sort(Comparator.comparingInt(LeaderboardEntry::getRank));

        return new PromptQualitySummary(
                entries.size(),
                best.getVersionId(), best.getScore(),
                worst.getVersionId(), worst.getScore(),
                average, best.getScore() - worst.getScore(),
                standings);
    }
}
