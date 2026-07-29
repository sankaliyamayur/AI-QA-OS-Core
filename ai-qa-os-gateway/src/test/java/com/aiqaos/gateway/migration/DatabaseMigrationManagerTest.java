package com.aiqaos.gateway.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseMigrationManagerTest {

    @Test
    @DisplayName("ORG-2: Should return DISABLED summary when Flyway bean is absent")
    void testDisabledFlywaySummary() {
        DatabaseMigrationManager manager = new DatabaseMigrationManager(
                new TestingObjectProvider<>(null),
                new TestingObjectProvider<>(null)
        );

        DatabaseMigrationManager.MigrationSummary summary = manager.getMigrationSummary();
        assertNotNull(summary);
        assertEquals("DISABLED", summary.getCurrentVersion());
        assertEquals(0, summary.getAppliedCount());
        assertEquals("N/A", summary.getStatus());
    }

    @Test
    @DisplayName("ORG-2: MigrationSummary DTO getters return expected values")
    void testMigrationSummaryDTO() {
        DatabaseMigrationManager.MigrationSummary summary = new DatabaseMigrationManager.MigrationSummary("15", 15, "SUCCESS");
        assertEquals("15", summary.getCurrentVersion());
        assertEquals(15, summary.getAppliedCount());
        assertEquals("SUCCESS", summary.getStatus());
    }

    @Test
    @DisplayName("ORG-2: ApplicationReady event handles missing Flyway gracefully")
    void testApplicationReadyWithNoFlyway() {
        DatabaseMigrationManager manager = new DatabaseMigrationManager(
                new TestingObjectProvider<>(null),
                new TestingObjectProvider<>(null)
        );

        assertDoesNotThrow(manager::onApplicationReady);
    }

    // Testing helper for ObjectProvider
    private static class TestingObjectProvider<T> implements ObjectProvider<T> {
        private final T instance;
        TestingObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }
}
