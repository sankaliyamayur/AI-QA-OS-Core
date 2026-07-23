package com.aiqaos.eval.benchmark;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Minimal boot config for the opt-in live benchmark ({@link PromptBenchmarkLiveTest}). Scans the
 * eval (non-persistence) packages plus {@code ai-provider} to supply {@code LLMProviderManager};
 * JPA/DataSource auto-config is excluded because the live benchmark needs no database (MOD-3's
 * persistence is optional via {@code ObjectProvider}). Only loaded when the live test is enabled.
 */
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class},
        scanBasePackages = {
                "com.aiqaos.eval.benchmark",
                "com.aiqaos.eval.config",
                "com.aiqaos.eval.service",
                "com.aiqaos.eval.evaluator",
                "com.aiqaos.eval.harness",
                "com.aiqaos.eval.dataset",
                "com.aiqaos.provider"
        })
public class EvalTestApplication {
}
