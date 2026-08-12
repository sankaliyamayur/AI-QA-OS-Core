package com.aiqaos.agent.entity;

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
 * {@code agent_executions.request_payload} and {@code response_payload} must map to {@code text},
 * never to PostgreSQL large objects.
 *
 * <p>Dormant today: {@code AgentExecutionRepository} has no production callers and the table is
 * empty, so nothing has ever written or read these. They are converted with the rest so that the
 * defect cannot wake up later — the first component to use this repository would otherwise inherit
 * both the leak and the read failure.
 *
 * <p>See {@code AgentTraceLobMappingTest} in ai-qa-os-observability for the full root cause and for
 * why the resolved mapping is asserted rather than a live query.
 */
class AgentExecutionLobMappingTest {

    private static String resolvedSqlType(String propertyName) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(BaseEntity.class)
                    .addAnnotatedClass(AgentExecutionEntity.class)
                    .buildMetadata();
            PersistentClass binding = metadata.getEntityBinding(AgentExecutionEntity.class.getName());
            Column column = (Column) binding.getProperty(propertyName).getSelectables().get(0);
            return column.getSqlType(metadata).toLowerCase();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void requestPayloadMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("request"),
                "request_payload must be text, not an oid large object");
    }

    @Test
    void responsePayloadMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("response"),
                "response_payload must be text, not an oid large object");
    }
}
