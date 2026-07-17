-- V3/V4 declared @Lob String columns as TEXT, but Hibernate's PostgreSQLDialect maps @Lob
-- String properties to the oid (CLOB) JDBC type by default, which fails schema-validation
-- against a plain TEXT column. Fix the two pre-existing offenders (tables are empty in fresh
-- dev databases, so drop+recreate is safe here instead of a data-preserving USING cast).

ALTER TABLE observability_events DROP COLUMN payload;
ALTER TABLE observability_events ADD COLUMN payload OID;

ALTER TABLE agent_traces DROP COLUMN prompt;
ALTER TABLE agent_traces ADD COLUMN prompt OID;

ALTER TABLE agent_traces DROP COLUMN response;
ALTER TABLE agent_traces ADD COLUMN response OID;
