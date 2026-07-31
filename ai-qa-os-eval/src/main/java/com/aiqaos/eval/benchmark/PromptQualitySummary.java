package com.aiqaos.eval.benchmark;

import java.util.Collections;
import java.util.List;

/**
 * PE-3: the prompt-quality read-model — the leaderboard standings plus at-a-glance stats (best/worst
 * version, average score, and the spread between them) that prompt engineers see on the dashboard.
 * Composed from PE-2's {@link LeaderboardEntry} rows.
 */
public final class PromptQualitySummary {

    private final int totalVersions;
    private final String bestVersionId;
    private final double bestScore;
    private final String worstVersionId;
    private final double worstScore;
    private final double averageScore;
    private final double scoreSpread;         // bestScore - worstScore
    private final List<LeaderboardEntry> standings;

    public PromptQualitySummary(int totalVersions, String bestVersionId, double bestScore,
                                String worstVersionId, double worstScore, double averageScore,
                                double scoreSpread, List<LeaderboardEntry> standings) {
        this.totalVersions = totalVersions;
        this.bestVersionId = bestVersionId;
        this.bestScore = bestScore;
        this.worstVersionId = worstVersionId;
        this.worstScore = worstScore;
        this.averageScore = averageScore;
        this.scoreSpread = scoreSpread;
        this.standings = standings != null
                ? Collections.unmodifiableList(List.copyOf(standings)) : List.of();
    }

    public static PromptQualitySummary empty() {
        return new PromptQualitySummary(0, null, 0.0, null, 0.0, 0.0, 0.0, List.of());
    }

    public int getTotalVersions() { return totalVersions; }
    public String getBestVersionId() { return bestVersionId; }
    public double getBestScore() { return bestScore; }
    public String getWorstVersionId() { return worstVersionId; }
    public double getWorstScore() { return worstScore; }
    public double getAverageScore() { return averageScore; }
    public double getScoreSpread() { return scoreSpread; }
    public List<LeaderboardEntry> getStandings() { return standings; }
}
