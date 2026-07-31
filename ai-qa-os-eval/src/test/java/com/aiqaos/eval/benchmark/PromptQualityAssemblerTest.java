package com.aiqaos.eval.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

/** PE-3: unit tests for composing the PE-2 leaderboard into a prompt-quality summary. No Mockito. */
class PromptQualityAssemblerTest {

    private final PromptQualityAssembler assembler = new PromptQualityAssembler();

    @Test
    void summarizesBestWorstAverageAndSpread() {
        PromptQualitySummary s = assembler.summarize(List.of(
                new LeaderboardEntry("v-top", 0.90, 1),
                new LeaderboardEntry("v-mid", 0.70, 2),
                new LeaderboardEntry("v-low", 0.50, 3)));

        assertThat(s.getTotalVersions()).isEqualTo(3);
        assertThat(s.getBestVersionId()).isEqualTo("v-top");
        assertThat(s.getBestScore()).isEqualTo(0.90);
        assertThat(s.getWorstVersionId()).isEqualTo("v-low");
        assertThat(s.getWorstScore()).isEqualTo(0.50);
        assertThat(s.getAverageScore()).isCloseTo(0.70, within(1e-9));
        assertThat(s.getScoreSpread()).isCloseTo(0.40, within(1e-9));
    }

    @Test
    void standingsAreOrderedByRank() {
        PromptQualitySummary s = assembler.summarize(List.of(
                new LeaderboardEntry("v-low", 0.50, 3),
                new LeaderboardEntry("v-top", 0.90, 1),
                new LeaderboardEntry("v-mid", 0.70, 2)));
        assertThat(s.getStandings()).extracting(LeaderboardEntry::getVersionId)
                .containsExactly("v-top", "v-mid", "v-low");
    }

    @Test
    void singleEntryHasZeroSpreadAndIsBothBestAndWorst() {
        PromptQualitySummary s = assembler.summarize(List.of(new LeaderboardEntry("only", 0.8, 1)));
        assertThat(s.getBestVersionId()).isEqualTo("only");
        assertThat(s.getWorstVersionId()).isEqualTo("only");
        assertThat(s.getScoreSpread()).isZero();
    }

    @Test
    void emptyLeaderboardYieldsEmptySummary() {
        PromptQualitySummary s = assembler.summarize(List.of());
        assertThat(s.getTotalVersions()).isZero();
        assertThat(s.getBestVersionId()).isNull();
        assertThat(s.getStandings()).isEmpty();
    }
}
