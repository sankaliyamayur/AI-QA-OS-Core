package com.aiqaos.execution.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.core.model.GeneratedScriptSuite;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The tests directory must never contain anything but the current run's scripts.
 *
 * <p><b>The false green this closes.</b> {@code writeScriptFiles} cleaned the directory, but only
 * after an early return that fired when the script suite was empty — so on an empty suite the
 * previous run's {@code .spec} files survived, Playwright executed them, and their results were
 * reported as this run's. A live Ollama-backed run "passed 2 tests" that had been written six hours
 * earlier by an unrelated simulator run, while generating nothing itself.
 *
 * <p>Cleaning is now unconditional. An empty suite leaves an empty directory and Playwright fails
 * with "no tests found", which is the honest outcome: the only safe thing to execute is what this
 * run produced.
 */
class StaleTestsDirectoryTest {

    /** Lays out scripts/run-playwright.ps1 + scripts/tests/<stale files>, returns the script path. */
    private static String layout(Path root, String... staleSpecNames) throws IOException {
        Path scripts = root.resolve("scripts");
        Path tests = scripts.resolve("tests");
        Files.createDirectories(tests);
        Path runner = scripts.resolve("run-playwright.ps1");
        Files.writeString(runner, "# runner");
        for (String name : staleSpecNames) {
            Files.writeString(tests.resolve(name), "// left over from an earlier run");
        }
        return runner.toString();
    }

    private static List<String> specsIn(Path root) throws IOException {
        Path tests = root.resolve("scripts").resolve("tests");
        if (!Files.isDirectory(tests)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(tests)) {
            return s.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    private static GeneratedScriptSuite suiteWith(String testCaseId, String code) {
        GeneratedScriptSuite.AutomationScript script = new GeneratedScriptSuite.AutomationScript();
        script.setScriptId("sc-1");
        script.setTestCaseId(testCaseId);
        script.setCode(code);
        GeneratedScriptSuite suite = new GeneratedScriptSuite();
        suite.setSuiteId("suite-1");
        suite.setScripts(new java.util.ArrayList<>(List.of(script)));
        return suite;
    }

    @Test
    @DisplayName("an EMPTY suite must still clear the directory — this is the bug")
    void emptySuiteClearsStaleSpecs(@TempDir Path root) throws Exception {
        String scriptPath = layout(root, "TC-001.spec.ts", "TC-002.spec.ts");
        assertEquals(2, specsIn(root).size(), "precondition: stale specs are present");

        new PlaywrightExecutionEngine().writeScriptFiles(scriptPath, new GeneratedScriptSuite());

        assertTrue(specsIn(root).isEmpty(),
                "stale specs survived an empty suite — Playwright would run a previous run's tests "
                        + "and report them as this run's: " + specsIn(root));
        assertTrue(Files.isDirectory(root.resolve("scripts").resolve("tests")),
                "the directory itself must remain so Playwright reports 'no tests found' rather than "
                        + "failing on a missing testDir");
    }

    @Test
    @DisplayName("a null suite is treated the same way")
    void nullSuiteAlsoClears(@TempDir Path root) throws Exception {
        String scriptPath = layout(root, "TC-001.spec.ts");

        new PlaywrightExecutionEngine().writeScriptFiles(scriptPath, null);

        assertTrue(specsIn(root).isEmpty(), "a null suite must not leave stale specs behind");
    }

    @Test
    @DisplayName("a real suite replaces the stale specs rather than joining them")
    void generatedScriptsReplaceStaleOnes(@TempDir Path root) throws Exception {
        String scriptPath = layout(root, "TC-OLD-1.spec.ts", "TC-OLD-2.spec.ts");

        new PlaywrightExecutionEngine()
                .writeScriptFiles(scriptPath, suiteWith("TC-NEW", "import { test } from '@playwright/test';"));

        List<String> specs = specsIn(root);
        assertEquals(List.of("TC-NEW.spec.ts"), specs,
                "the directory must hold exactly this run's scripts: " + specs);
        assertFalse(specs.contains("TC-OLD-1.spec.ts"));
        assertEquals("import { test } from '@playwright/test';",
                Files.readString(root.resolve("scripts").resolve("tests").resolve("TC-NEW.spec.ts")));
    }
}
