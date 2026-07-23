package com.aiqaos.eval.evaluator;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for the LLM judge — score parsing and the evaluate() path via a stub judge. */
class LlmJudgeEvaluatorTest {

    private static EvaluationCase sampleCase() {
        return new EvaluationCase("c1", "input", null, List.of("must be polite"), List.of());
    }

    @Test
    void parseScore_readsPlainFloat() {
        assertThat(LlmJudgeEvaluator.parseScore("0.85")).isEqualTo(0.85);
    }

    @Test
    void parseScore_extractsNumberFromProse() {
        assertThat(LlmJudgeEvaluator.parseScore("Score: 0.7 because ...")).isEqualTo(0.7);
    }

    @Test
    void parseScore_treatsOutOfRangeAsPercentage() {
        assertThat(LlmJudgeEvaluator.parseScore("90")).isEqualTo(0.9);
    }

    @Test
    void parseScore_clampsAndDefaults() {
        assertThat(LlmJudgeEvaluator.parseScore("no number here")).isEqualTo(0.0);
        assertThat(LlmJudgeEvaluator.parseScore(null)).isEqualTo(0.0);
    }

    @Test
    void evaluate_passesWhenJudgeScoresHigh() {
        LlmJudgeEvaluator judge = new LlmJudgeEvaluator((system, user) -> "0.95");
        EvaluationResult r = judge.evaluate(sampleCase(), "some output");
        assertThat(r.getScore()).isEqualTo(0.95);
        assertThat(r.isPassed()).isTrue();
    }

    @Test
    void evaluate_failsGracefullyWhenJudgeThrows() {
        LlmJudgeEvaluator judge = new LlmJudgeEvaluator((system, user) -> {
            throw new IllegalStateException("provider down");
        });
        EvaluationResult r = judge.evaluate(sampleCase(), "some output");
        assertThat(r.isPassed()).isFalse();
        assertThat(r.getReason()).contains("judge call failed");
    }
}
