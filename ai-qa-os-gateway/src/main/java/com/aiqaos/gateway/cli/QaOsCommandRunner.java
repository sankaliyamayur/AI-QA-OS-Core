package com.aiqaos.gateway.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CLI entry point for AI-QA-OS developer commands.
 *
 * Usage examples:
 *   qaos brain analyze --input story.txt
 *   qaos workflow start --file login-regression.yaml
 *   qaos execution status --id 12345
 *   qaos report download --id 12345 --format PDF
 */
@Component
public class QaOsCommandRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QaOsCommandRunner.class);

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) return;
        if (!"qaos".equals(args[0])) return;

        if (args.length < 2) {
            printHelp();
            return;
        }

        String command = args[1];
        switch (command) {
            case "brain"     -> handleBrain(args);
            case "workflow"  -> handleWorkflow(args);
            case "execution" -> handleExecution(args);
            case "report"    -> handleReport(args);
            case "agent"     -> handleAgent(args);
            default          -> printHelp();
        }
    }

    private void handleBrain(String[] args)     { log.info("[CLI] Brain: {}", String.join(" ", args)); }
    private void handleWorkflow(String[] args)  { log.info("[CLI] Workflow: {}", String.join(" ", args)); }
    private void handleExecution(String[] args) { log.info("[CLI] Execution: {}", String.join(" ", args)); }
    private void handleReport(String[] args)    { log.info("[CLI] Report: {}", String.join(" ", args)); }
    private void handleAgent(String[] args)     { log.info("[CLI] Agent: {}", String.join(" ", args)); }

    private void printHelp() {
        System.out.println("AI-QA-OS CLI");
        System.out.println("  qaos brain analyze --input <file>");
        System.out.println("  qaos workflow start --file <yaml>");
        System.out.println("  qaos workflow list");
        System.out.println("  qaos execution status --id <id>");
        System.out.println("  qaos agent start --type <type>");
        System.out.println("  qaos report download --id <id> --format PDF");
    }
}