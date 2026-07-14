package com.aiqaos.execution.engine;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedScriptSuite.AutomationScript;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ExecutionEngineTest {

    @Test
    public void testExecutionEngineFactoryRegistration() {
        PlaywrightExecutionEngine engine = new PlaywrightExecutionEngine();
        ExecutionEngineFactory factory = new ExecutionEngineFactory(List.of(engine));

        ExecutionEngine resolved = factory.getEngine("Playwright");
        assertNotNull(resolved);
        assertEquals("Playwright", resolved.getSupportedFramework());

        assertThrows(IllegalArgumentException.class, () -> {
            factory.getEngine("Selenium");
        });
    }

    @Test
    public void testPlaywrightExecutionEngineSuccess() {
        PlaywrightExecutionEngine engine = new PlaywrightExecutionEngine();

        GeneratedScriptSuite suite = new GeneratedScriptSuite();
        suite.setSuiteId("suite-123");
        
        AutomationScript script1 = new AutomationScript();
        script1.setScriptId("script-1");
        script1.setCode("// Successful Playwright command");
        script1.setFramework("Playwright");
        
        suite.setScripts(List.of(script1));

        ExecutionConfiguration config = new ExecutionConfiguration();
        config.setBrowser(BrowserType.CHROME);
        config.setHeadless(true);

        assertFalse(engine.isRunning());
        ExecutionResult result = engine.execute(suite, config);
        assertFalse(engine.isRunning());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("PASSED", result.getStatus());
        assertEquals(1, result.getPassed());
        assertEquals(0, result.getFailed());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertTrue(result.getDuration() >= 0);
        assertNotNull(result.getConsoleLogs());
        assertTrue(result.getConsoleLogs().contains("Launching browser: CHROME"));
    }

    @Test
    public void testPlaywrightExecutionEngineFailFastOnUnsupportedOperationException() {
        PlaywrightExecutionEngine engine = new PlaywrightExecutionEngine();

        GeneratedScriptSuite suite = new GeneratedScriptSuite();
        suite.setSuiteId("suite-123");
        
        AutomationScript script1 = new AutomationScript();
        script1.setScriptId("script-1");
        script1.setCode("throw new UnsupportedOperationException(\"LLM did not generate automation code.\");");
        script1.setFramework("Playwright");
        
        suite.setScripts(List.of(script1));

        ExecutionConfiguration config = new ExecutionConfiguration();

        ExecutionResult result = engine.execute(suite, config);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());
        assertEquals(0, result.getPassed());
        assertEquals(1, result.getFailed());
        assertEquals("Automation script runtime exception: UnsupportedOperationException", result.getErrorMessage());
        assertNotNull(result.getStackTrace());
    }

    @Test
    public void testPlaywrightExecutionEngineCancel() {
        PlaywrightExecutionEngine engine = new PlaywrightExecutionEngine();
        assertFalse(engine.isRunning());
        engine.cancel();
        assertFalse(engine.isRunning());
    }
}
