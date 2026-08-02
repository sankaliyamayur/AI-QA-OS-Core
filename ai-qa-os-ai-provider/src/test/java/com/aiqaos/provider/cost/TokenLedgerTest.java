package com.aiqaos.provider.cost;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** AI-6 (ADR-075): token accumulation per scope + daily rollover of the global counter. */
class TokenLedgerTest {

    @Test
    void accumulatesPerWorkflowAgentAndGlobal() {
        TokenLedger ledger = new TokenLedger();

        ledger.record(100, "wf-1", "SCRIPT_GENERATOR");
        ledger.record(250, "wf-1", "BUG_ANALYZER");
        ledger.record(400, "wf-2", "SCRIPT_GENERATOR");

        assertThat(ledger.workflow("wf-1")).isEqualTo(350);
        assertThat(ledger.workflow("wf-2")).isEqualTo(400);
        assertThat(ledger.agent("SCRIPT_GENERATOR")).isEqualTo(500);
        assertThat(ledger.globalToday()).isEqualTo(750);
    }

    @Test
    void nullKeysAreSafe() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(123, null, null);   // still counts globally
        assertThat(ledger.globalToday()).isEqualTo(123);
        assertThat(ledger.workflow(null)).isZero();
        assertThat(ledger.agent(null)).isZero();
    }

    @Test
    void globalCounterRollsOverDaily() {
        AtomicReference<LocalDate> today = new AtomicReference<>(LocalDate.of(2026, 8, 2));
        TokenLedger ledger = new TokenLedger(today::get);

        ledger.record(5000, "wf-1", "A");
        assertThat(ledger.globalToday()).isEqualTo(5000);

        today.set(LocalDate.of(2026, 8, 3));   // next day
        assertThat(ledger.globalToday()).isZero();   // global reset

        ledger.record(1000, "wf-2", "A");
        assertThat(ledger.globalToday()).isEqualTo(1000);
    }
}
