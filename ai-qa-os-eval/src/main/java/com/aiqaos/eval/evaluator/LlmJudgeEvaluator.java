package com.aiqaos.eval.evaluator;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import com.aiqaos.eval.contract.Evaluator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * LLM-as-judge evaluator: asks a model to score how well the output meets the case criteria.
 *
 * <p>The model call is behind the {@link JudgeLlm} seam so the prompt-building and
 * score-parsing are unit-testable without a live LLM. The production {@link JudgeLlm}
 * ({@link LlmProviderJudge}) wraps {@code LLMProviderManager}. This is intentionally
 * minimal — AI-3/PE-1 enrich the rubric and add consistency/self-agreement judging.
 */
@Component
public class LlmJudgeEvaluator implements Evaluator {

    /** Seam for the judge model call. Prod = {@link LlmProviderJudge}; tests pass a stub. */
    public interface JudgeLlm {
        String judge(String systemPrompt, String userPrompt);
    }

    private static final Pattern NUMBER = Pattern.compile("(\\d*\\.?\\d+)");

    private final JudgeLlm judge;

    public LlmJudgeEvaluator(JudgeLlm judge) {
        this.judge = judge;
    }

    @Override
    public String getName() {
        return "llm-judge";
    }

    @Override
    public EvaluationResult evaluate(EvaluationCase testCase, String actualOutput) {
        String system = "You are a strict QA evaluator. Score how well the OUTPUT satisfies the "
                + "CRITERIA on a scale from 0.0 to 1.0. Reply with only the number.";
        String user = "CRITERIA:\n" + String.join("\n", testCase.getCriteria())
                + "\n\nEXPECTED (optional):\n" + nullToEmpty(testCase.getExpectedOutput())
                + "\n\nOUTPUT:\n" + nullToEmpty(actualOutput);

        String response;
        try {
            response = judge.judge(system, user);
        } catch (Exception e) {
            return new EvaluationResult(getName(), 0.0, false, "judge call failed: " + e.getMessage());
        }
        double score = parseScore(response);
        boolean passed = score >= 0.7;
        return new EvaluationResult(getName(), score, passed, "judge score " + score);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Extract the first number from the judge response and normalise to {@code [0,1]}.
     * A value in {@code (1,100]} is treated as a percentage. Package-visible for tests.
     */
    static double parseScore(String response) {
        if (response == null) {
            return 0.0;
        }
        Matcher m = NUMBER.matcher(response);
        if (!m.find()) {
            return 0.0;
        }
        double v;
        try {
            v = Double.parseDouble(m.group(1));
        } catch (NumberFormatException e) {
            return 0.0;
        }
        if (v > 1.0 && v <= 100.0) {
            v = v / 100.0;
        }
        return Math.max(0.0, Math.min(1.0, v));
    }
}
