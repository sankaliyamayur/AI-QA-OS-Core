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
 * {@code observability_events.payload} must map to {@code text}, never to a PostgreSQL large object.
 *
 * <p>Counterpart of {@link AgentTraceLobMappingTest}, for the second column V6 converted to OID. This
 * one had not failed yet only because nothing reads it — {@code EventProcessor} calls {@code save()}
 * and there is no read path — but it was still creating one large object per event and leaking it on
 * delete, since a large object outlives its row. An audit of the live database found 631 large
 * objects, 507 referenced by this column and 124 already orphaned.
 *
 * <p>The moment any read of {@code payload} is added outside a transaction it would fail exactly as
 * {@code agent_traces} did, with {@code PSQLException: Large Objects may not be used in auto-commit
 * mode}. See {@link AgentTraceLobMappingTest} for why the mapping is asserted rather than a query.
 */
class EventEntityLobMappingTest {

    private static String resolvedSqlType(String propertyName) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(BaseEntity.class)
                    .addAnnotatedClass(EventEntity.class)
                    .buildMetadata();
            PersistentClass binding = metadata.getEntityBinding(EventEntity.class.getName());
            Column column = (Column) binding.getProperty(propertyName).getSelectables().get(0);
            return column.getSqlType(metadata).toLowerCase();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void payloadMustNotMapToALargeObject() {
        assertEquals("text", resolvedSqlType("payload"),
                "payload must be text; oid leaks a large object per event and would fail any read "
                        + "outside a transaction");
    }
}
