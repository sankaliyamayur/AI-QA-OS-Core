package com.aiqaos.intelligence.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import com.aiqaos.intelligence.repository.PromptExecutionRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * FI-PE3-C: the producer for PE-3's per-execution prompt history. Every recorded field must be
 * observed rather than derived — an absent correlation is stored as null, never invented (ADR-063).
 */
class PromptExecutionRecorderTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void recordsTheRenderThatActuallyHappened() {
        List<PromptExecutionEntity> saved = new ArrayList<>();
        PromptExecutionRecorder recorder = new PromptExecutionRecorder(repoCapturing(saved), 20_000);

        recorder.record("BUG_ANALYSIS", "latest", "compiled prompt text", 42L);

        assertThat(saved).hasSize(1);
        PromptExecutionEntity e = saved.get(0);
        assertThat(e.getTemplateName()).isEqualTo("BUG_ANALYSIS");
        assertThat(e.getVersionLabel()).isEqualTo("latest");
        assertThat(e.getFinalCompiledPrompt()).isEqualTo("compiled prompt text");
        assertThat(e.getResponseTimeMs()).isEqualTo(42L);
    }

    @Test
    void tiesTheRenderToTheWorkflowRunViaTheMdcCorrelationId() {
        List<PromptExecutionEntity> saved = new ArrayList<>();
        MDC.put(PromptExecutionRecorder.MDC_CORRELATION_ID, "corr-123");
        MDC.put(PromptExecutionRecorder.MDC_TRACE_ID, "trace-abc");

        new PromptExecutionRecorder(repoCapturing(saved), 20_000)
                .record("QA_ANALYSIS", "latest", "text", 5L);

        assertThat(saved.get(0).getCorrelationId()).isEqualTo("corr-123");
        assertThat(saved.get(0).getTraceId()).isEqualTo("trace-abc");
    }

    @Test
    void outsideAPipelineRunTheCorrelationIsNullNotInvented() {
        List<PromptExecutionEntity> saved = new ArrayList<>();

        new PromptExecutionRecorder(repoCapturing(saved), 20_000)
                .record("QA_ANALYSIS", "latest", "text", 5L);

        assertThat(saved.get(0).getCorrelationId()).isNull();
        assertThat(saved.get(0).getTraceId()).isNull();
    }

    @Test
    void truncatesAnOversizedPromptAndSaysSo() {
        List<PromptExecutionEntity> saved = new ArrayList<>();
        String huge = "x".repeat(500);

        new PromptExecutionRecorder(repoCapturing(saved), 100).record("T", "v1", huge, 1L);

        String stored = saved.get(0).getFinalCompiledPrompt();
        assertThat(stored).startsWith("x".repeat(100));
        assertThat(stored).contains("truncated 400 chars");
        assertThat(stored.length()).isLessThan(huge.length());
    }

    @Test
    void keepsAPromptThatFitsIntact() {
        List<PromptExecutionEntity> saved = new ArrayList<>();
        String prompt = "y".repeat(100);

        new PromptExecutionRecorder(repoCapturing(saved), 100).record("T", "v1", prompt, 1L);

        assertThat(saved.get(0).getFinalCompiledPrompt()).isEqualTo(prompt);
    }

    @Test
    void aNullPromptIsStoredAsEmptyBecauseTheColumnIsNotNull() {
        List<PromptExecutionEntity> saved = new ArrayList<>();

        new PromptExecutionRecorder(repoCapturing(saved), 20_000).record("T", "v1", null, 1L);

        assertThat(saved.get(0).getFinalCompiledPrompt()).isEmpty();
    }

    @Test
    void aFailingRepositoryNeverBreaksTheRender() {
        PromptExecutionRecorder recorder = new PromptExecutionRecorder(failingRepo(), 20_000);

        // history is diagnostics — losing a row must never fail the prompt render that produced it
        assertThatCode(() -> recorder.record("T", "v1", "text", 1L)).doesNotThrowAnyException();
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static PromptExecutionRepository repoCapturing(List<PromptExecutionEntity> sink) {
        return proxy((method, args) -> {
            if ("save".equals(method.getName())) {
                sink.add((PromptExecutionEntity) args[0]);
                return args[0];
            }
            return defaultFor(method.getReturnType());
        });
    }

    private static PromptExecutionRepository failingRepo() {
        return proxy((method, args) -> {
            if ("save".equals(method.getName())) {
                throw new IllegalStateException("database unavailable");
            }
            return defaultFor(method.getReturnType());
        });
    }

    private interface Handler {
        Object handle(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }

    private static PromptExecutionRepository proxy(Handler handler) {
        return (PromptExecutionRepository) Proxy.newProxyInstance(
                PromptExecutionRecorderTest.class.getClassLoader(),
                new Class<?>[]{PromptExecutionRepository.class},
                (p, method, args) -> handler.handle(method, args));
    }

    private static Object defaultFor(Class<?> returnType) {
        if (returnType == boolean.class) return false;
        if (returnType == long.class) return 0L;
        if (returnType == Optional.class) return Optional.empty();
        if (returnType == List.class) return List.of();
        return null;
    }
}
