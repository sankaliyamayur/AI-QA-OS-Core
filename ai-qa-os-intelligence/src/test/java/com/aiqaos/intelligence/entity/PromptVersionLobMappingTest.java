package com.aiqaos.intelligence.entity;

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
 * {@code prompt_versions.content} must map to {@code text}, never to a PostgreSQL large object.
 *
 * <p><b>This was the most dangerous of the remaining @Lob columns.</b> Unlike the others it has a
 * live read path — {@code DbPromptSource} resolves a version and calls {@code getContent()} — so it
 * was a latent HTTP 500 waiting for the first row. The table is empty today only because prompts
 * load from the classpath rather than the database (the same circumstance the V23 comment on
 * {@code PromptExecutionEntity} describes), so the failure had never been triggered.
 *
 * <p>Reading a large object outside a transaction fails with {@code PSQLException: Large Objects may
 * not be used in auto-commit mode} — the defect that took down {@code GET /api/dashboard/agents/traces}.
 */
class PromptVersionLobMappingTest {

    private static String resolvedSqlType(String propertyName) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(BaseEntity.class)
                    .addAnnotatedClass(PromptVersionEntity.class)
                    .buildMetadata();
            PersistentClass binding = metadata.getEntityBinding(PromptVersionEntity.class.getName());
            Column column = (Column) binding.getProperty(propertyName).getSelectables().get(0);
            return column.getSqlType(metadata).toLowerCase();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void contentMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("content"),
                "content must be text; DbPromptSource reads it, so oid makes the first stored prompt "
                        + "version fail with \"Large Objects may not be used in auto-commit mode\"");
    }
}
