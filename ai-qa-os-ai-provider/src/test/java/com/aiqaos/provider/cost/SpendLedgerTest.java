package com.aiqaos.provider.cost;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** ENT-3: spend accumulation per scope + daily rollover of the global counter. */
class SpendLedgerTest {

    @Test
    void accumulatesPerWorkflowAgentAndGlobal() {
        SpendLedger ledger = new SpendLedger();

        ledger.record(0.10, "wf-1", "SCRIPT_GENERATOR");
        ledger.record(0.25, "wf-1", "BUG_ANALYZER");
        ledger.record(0.40, "wf-2", "SCRIPT_GENERATOR");

        assertThat(ledger.workflow("wf-1")).isEqualTo(0.35);
        assertThat(ledger.workflow("wf-2")).isEqualTo(0.40);
        assertThat(ledger.agent("SCRIPT_GENERATOR")).isEqualTo(0.50);
        assertThat(ledger.globalToday()).isEqualTo(0.75);
    }

    @Test
    void globalCounterRollsOverDaily() {
        AtomicReference<LocalDate> today = new AtomicReference<>(LocalDate.of(2026, 7, 28));
        SpendLedger ledger = new SpendLedger(today::get);

        ledger.record(5.0, "wf-1", "A");
        assertThat(ledger.globalToday()).isEqualTo(5.0);

        today.set(LocalDate.of(2026, 7, 29));   // next day
        assertThat(ledger.globalToday()).isEqualTo(0.0);   // global reset

        ledger.record(1.0, "wf-2", "A");
        assertThat(ledger.globalToday()).isEqualTo(1.0);
    }
}
