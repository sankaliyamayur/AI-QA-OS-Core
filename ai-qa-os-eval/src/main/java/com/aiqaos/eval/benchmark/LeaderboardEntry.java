package com.aiqaos.eval.benchmark;

/**
 * PE-2: one row of the prompt leaderboard — a prompt version, its measured score, and its rank
 * (1 = best).
 */
public class LeaderboardEntry {

    private final String versionId;
    private final double score;
    private final int rank;

    public LeaderboardEntry(String versionId, double score, int rank) {
        this.versionId = versionId;
        this.score = score;
        this.rank = rank;
    }

    public String getVersionId() {
        return versionId;
    }

    public double getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }
}
