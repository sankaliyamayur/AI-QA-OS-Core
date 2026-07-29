package com.aiqaos.gateway.cli;

import com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class QaOsCommandRunnerTest {

    private QaOsCommandRunner runner;

    @BeforeEach
    void setUp() {
        runner = new QaOsCommandRunner(
                new TestingObjectProvider<>(null),
                new TestingObjectProvider<>(null),
                new TestingObjectProvider<>(null)
        );
    }

    @Test
    @DisplayName("DX-1: qaos doctor command executes without exception")
    void testDoctorCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "doctor"));
    }

    @Test
    @DisplayName("DX-1: qaos version command executes without exception")
    void testVersionCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "version"));
    }

    @Test
    @DisplayName("DX-1: qaos workflow commands (list, run) execute without exception")
    void testWorkflowCommands() {
        assertDoesNotThrow(() -> runner.run("qaos", "workflow", "list"));
        assertDoesNotThrow(() -> runner.run("qaos", "workflow", "run", "--name", "TEST_SUITE"));
        assertDoesNotThrow(() -> runner.run("qaos", "workflow", "list", "--json"));
    }

    @Test
    @DisplayName("DX-1: qaos execution status command executes without exception")
    void testExecutionCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "execution", "status", "--id", "exec-101"));
        assertDoesNotThrow(() -> runner.run("qaos", "execution", "status", "--id", "exec-101", "--json"));
    }

    @Test
    @DisplayName("DX-1: qaos agent list command executes without exception")
    void testAgentCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "agent", "list"));
        assertDoesNotThrow(() -> runner.run("qaos", "agent", "list", "--json"));
    }

    @Test
    @DisplayName("DX-2: qaos generate command executes without exception for all types")
    void testGenerateCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "generate", "agent", "--name", "CustomTest", "--dir", "target/test-scaffold"));
        assertDoesNotThrow(() -> runner.run("qaos", "generate", "workflow", "--name", "CustomWorkflow", "--dir", "target/test-scaffold"));
        assertDoesNotThrow(() -> runner.run("qaos", "generate", "prompt", "--name", "CustomPrompt", "--dir", "target/test-scaffold"));
        assertDoesNotThrow(() -> runner.run("qaos", "generate", "module", "--name", "CustomModule", "--dir", "target/test-scaffold"));
        assertDoesNotThrow(() -> runner.run("qaos", "generate", "docs", "--dir", "target/test-docs"));
    }

    @Test
    @DisplayName("DX-1: qaos report show command executes without exception")
    void testReportCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "report", "show", "--id", "exec-101"));
    }

    @Test
    @DisplayName("DX-1: qaos help command executes without exception")
    void testHelpCommand() {
        assertDoesNotThrow(() -> runner.run("qaos", "help"));
        assertDoesNotThrow(() -> runner.run("qaos", "--help"));
        assertDoesNotThrow(() -> runner.run());
    }

    // Stub ObjectProvider for unit testing
    private static class TestingObjectProvider<T> implements org.springframework.beans.factory.ObjectProvider<T> {
        private final T instance;
        TestingObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }
}
