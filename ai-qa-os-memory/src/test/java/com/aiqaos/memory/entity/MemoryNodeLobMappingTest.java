package com.aiqaos.memory.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aiqaos.core.entity.BaseEntity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.junit.jupiter.api.Test;

/**
 * {@code memory_nodes.content} must map to {@code text}, never to a PostgreSQL large object.
 *
 * <p>Write-only today — {@code MemoryPersistence} and {@code MemoryManagerImpl} both only
 * {@code save()} — so this had not failed, but every stored memory node was creating a large object
 * that outlives its row and leaks on delete. Memory is by nature long-lived and high-volume, so this
 * is the mapping most likely to accumulate orphans over time.
 *
 * <p>Any future read of {@code content} outside a transaction would fail with
 * {@code PSQLException: Large Objects may not be used in auto-commit mode}. See
 * {@code AgentTraceLobMappingTest} in ai-qa-os-observability for why the mapping is asserted rather
 * than a query: H2 has no large-object type and there is no Docker here for Testcontainers.
 */
class MemoryNodeLobMappingTest {

    private static String resolvedSqlType(String propertyName) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(BaseEntity.class)
                    .addAnnotatedClass(MemoryNodeEntity.class)
                    .buildMetadata();
            PersistentClass binding = metadata.getEntityBinding(MemoryNodeEntity.class.getName());
            Column column = (Column) binding.getProperty(propertyName).getSelectables().get(0);
            return column.getSqlType(metadata).toLowerCase();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void contentMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("content"),
                "content must be text; oid leaks a large object per memory node and would fail any "
                        + "read outside a transaction");
    }
}
