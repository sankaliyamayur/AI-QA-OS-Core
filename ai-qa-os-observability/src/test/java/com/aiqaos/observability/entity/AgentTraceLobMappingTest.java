package com.aiqaos.observability.entity;

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
 * {@code agent_traces.prompt} and {@code response} must map to {@code text}, never to a PostgreSQL
 * large object ({@code oid}).
 *
 * <p><b>The bug this pins down.</b> These fields were annotated {@code @Lob}, and Hibernate's
 * PostgreSQL dialect maps {@code @Lob String} to {@code oid} rather than {@code text}. V6 then made
 * the database agree by converting the columns from TEXT to OID, so schema validation passed and
 * writes worked — but large objects can only be read inside a transaction, so every read through
 * {@code GET /api/dashboard/agents/traces} failed:
 *
 * <pre>
 *   JpaSystemException: Unable to access lob stream
 *     -> HibernateException: Unable to access lob stream
 *       -> PSQLException: Large Objects may not be used in auto-commit mode.
 * </pre>
 *
 * <p>Large objects also outlive their row — deleting a trace leaks its two objects, which is why the
 * V23 comment on {@code PromptExecutionEntity.finalCompiledPrompt} chose TEXT over {@code @Lob}.
 *
 * <p><b>Why the mapping and not the endpoint is asserted here.</b> The failure needs a real
 * PostgreSQL server: H2 has no {@code oid} large-object type, so an H2 slice test cannot reproduce
 * it, and this machine has no Docker for Testcontainers. Building Hibernate's metadata against
 * {@link PostgreSQLDialect} resolves the exact column type Hibernate would emit and validate against,
 * which is the defect itself rather than a proxy for it. Live verification against real Postgres is
 * recorded in the fix's commit message.
 */
class AgentTraceLobMappingTest {

    private static String resolvedSqlType(String propertyName) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(BaseEntity.class)
                    .addAnnotatedClass(AgentTraceEntity.class)
                    .buildMetadata();
            PersistentClass binding = metadata.getEntityBinding(AgentTraceEntity.class.getName());
            Column column = (Column) binding.getProperty(propertyName).getSelectables().get(0);
            return column.getSqlType(metadata).toLowerCase();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void promptMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("prompt"),
                "prompt must be text; oid makes every read outside a transaction fail with "
                        + "\"Large Objects may not be used in auto-commit mode\"");
    }

    @Test
    void responseMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("response"),
                "response must be text; oid makes every read outside a transaction fail with "
                        + "\"Large Objects may not be used in auto-commit mode\"");
    }
}
