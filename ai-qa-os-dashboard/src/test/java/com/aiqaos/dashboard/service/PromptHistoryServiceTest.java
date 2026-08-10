package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.dashboard.dto.PromptHistoryEntryDTO;
import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import com.aiqaos.intelligence.repository.PromptExecutionRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/** FI-PE3-C: the per-execution prompt-history read-model over the newly-produced prompt_executions. */
class PromptHistoryServiceTest {

    @Test
    void mapsARecordedRenderToTheReadModel() {
        PromptExecutionEntity e = execution("BUG_ANALYSIS", "latest", "corr-1", "a".repeat(500));
        PromptHistoryService service = new PromptHistoryService(pagingRepo(List.of(e), null), 50, 500, 200);

        List<PromptHistoryEntryDTO> history = service.recent(null);

        assertEquals(1, history.size());
        PromptHistoryEntryDTO dto = history.get(0);
        assertEquals("BUG_ANALYSIS", dto.templateName());
        assertEquals("latest", dto.versionLabel());
        assertEquals("corr-1", dto.correlationId());
        assertEquals(500, dto.promptLength(), "reports the true prompt length, not the preview length");
        assertNotNull(dto.executedAt());
    }

    @Test
    void emitsAPreviewNotTheFullPromptBecauseTheEndpointIsUnauthenticated() {
        PromptExecutionEntity e = execution("T", "v1", "corr-1", "secret-context-".repeat(50));
        PromptHistoryService service = new PromptHistoryService(pagingRepo(List.of(e), null), 50, 500, 20);

        PromptHistoryEntryDTO dto = service.recent(null).get(0);

        assertEquals(21, dto.promptPreview().length(), "20 chars + the ellipsis marker");
        assertTrue(dto.promptPreview().endsWith("…"));
        assertTrue(dto.promptLength() > dto.promptPreview().length());
    }

    @Test
    void shortPromptsAreNotEllipsised() {
        PromptExecutionEntity e = execution("T", "v1", "corr-1", "short");
        PromptHistoryService service = new PromptHistoryService(pagingRepo(List.of(e), null), 50, 500, 200);

        assertEquals("short", service.recent(null).get(0).promptPreview());
    }

    @Test
    void clampsTheRequestedLimitToTheConfiguredMaximum() {
        AtomicReference<Pageable> asked = new AtomicReference<>();
        PromptHistoryService service = new PromptHistoryService(capturingPageable(asked), 50, 100, 200);

        service.recent(10_000);

        assertEquals(100, asked.get().getPageSize(), "a caller cannot ask for the whole table");
    }

    @Test
    void appliesTheDefaultLimitWhenNoneGiven() {
        AtomicReference<Pageable> asked = new AtomicReference<>();
        PromptHistoryService service = new PromptHistoryService(capturingPageable(asked), 50, 500, 200);

        service.recent(null);

        assertEquals(50, asked.get().getPageSize());
    }

    @Test
    void ordersNewestFirstAtTheDatabase() {
        AtomicReference<Pageable> asked = new AtomicReference<>();
        new PromptHistoryService(capturingPageable(asked), 50, 500, 200).recent(null);

        assertEquals("createdAt: DESC", asked.get().getSort().toString(),
                "sorting must be pushed to the DB — this table grows one row per render");
    }

    @Test
    void returnsEveryPromptOfOneWorkflowRun() {
        List<PromptExecutionEntity> run = List.of(
                execution("QA_ANALYSIS", "latest", "corr-run", "a"),
                execution("SCRIPT_GENERATION", "latest", "corr-run", "b"));
        PromptHistoryService service = new PromptHistoryService(pagingRepo(List.of(), run), 50, 500, 200);

        List<PromptHistoryEntryDTO> history = service.forCorrelation("corr-run");

        assertEquals(2, history.size());
        assertTrue(history.stream().allMatch(h -> "corr-run".equals(h.correlationId())));
    }

    @Test
    void blankCorrelationReturnsEmptyRatherThanEverything() {
        PromptHistoryService service = new PromptHistoryService(
                pagingRepo(List.of(execution("T", "v", "c", "p")), List.of()), 50, 500, 200);

        assertTrue(service.forCorrelation("  ").isEmpty(), "must not silently widen to all runs");
        assertTrue(service.forCorrelation(null).isEmpty());
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static PromptExecutionEntity execution(String template, String version,
                                                   String correlationId, String prompt) {
        PromptExecutionEntity e = new PromptExecutionEntity();
        e.setTemplateName(template);
        e.setVersionLabel(version);
        e.setCorrelationId(correlationId);
        e.setFinalCompiledPrompt(prompt);
        e.setResponseTimeMs(7L);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    private static PromptExecutionRepository pagingRepo(List<PromptExecutionEntity> page,
                                                        List<PromptExecutionEntity> byCorrelation) {
        return proxy((method, args) -> {
            if ("findAll".equals(method.getName()) && args != null && args.length == 1
                    && args[0] instanceof Pageable p) {
                return new PageImpl<>(page, p, page.size());
            }
            if ("findByCorrelationIdOrderByCreatedAtDesc".equals(method.getName())) {
                return byCorrelation == null ? List.of() : byCorrelation;
            }
            return defaultFor(method.getReturnType());
        });
    }

    private static PromptExecutionRepository capturingPageable(AtomicReference<Pageable> sink) {
        return proxy((method, args) -> {
            if ("findAll".equals(method.getName()) && args != null && args.length == 1
                    && args[0] instanceof Pageable p) {
                sink.set(p);
                return new PageImpl<>(List.of(), p, 0);
            }
            return defaultFor(method.getReturnType());
        });
    }

    private interface Handler {
        Object handle(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }

    private static PromptExecutionRepository proxy(Handler handler) {
        return (PromptExecutionRepository) Proxy.newProxyInstance(
                PromptHistoryServiceTest.class.getClassLoader(),
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
