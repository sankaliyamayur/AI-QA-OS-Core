-- V26: agent_traces.prompt / response — PostgreSQL large object (oid) back to TEXT.
--
-- V4 created these as TEXT. V6 converted them to OID so that Hibernate schema validation would
-- pass, because @Lob String maps to oid under the PostgreSQL dialect. That made the schema agree
-- with the mapping, but it made the data unreadable: a large object can only be read inside a
-- transaction, so writes succeeded while every read failed with
--
--   PSQLException: Large Objects may not be used in auto-commit mode.
--
-- surfacing as HTTP 500 "Unable to access lob stream" on GET /api/dashboard/agents/traces.
-- Large objects also outlive their row, so each deleted trace leaked two of them.
--
-- The mapping is fixed in AgentTraceEntity (@Column(columnDefinition = "TEXT")); this brings the
-- column type back in line with it. Same shape as V23, which chose TEXT for
-- prompt_executions.final_compiled_prompt for exactly this reason.
--
-- Data is preserved: each large object is read back with lo_get() before the column is replaced.

ALTER TABLE agent_traces ADD COLUMN prompt_text TEXT;
ALTER TABLE agent_traces ADD COLUMN response_text TEXT;

-- The EXISTS guard skips oids whose large object is already gone. A dangling reference would
-- otherwise abort the whole migration on lo_get(), and losing an unreadable value is preferable
-- to leaving the table unmigrated.
UPDATE agent_traces
   SET prompt_text = convert_from(lo_get(prompt), 'UTF8')
 WHERE prompt IS NOT NULL
   AND EXISTS (SELECT 1 FROM pg_largeobject_metadata m WHERE m.oid = agent_traces.prompt);

UPDATE agent_traces
   SET response_text = convert_from(lo_get(response), 'UTF8')
 WHERE response IS NOT NULL
   AND EXISTS (SELECT 1 FROM pg_largeobject_metadata m WHERE m.oid = agent_traces.response);

-- Free the large objects now that their contents live in the new columns. Without this the
-- conversion would orphan them permanently: dropping an oid column does not unlink what it points
-- to. Wrapped so that a permissions failure degrades to leaked objects rather than a failed
-- migration -- the schema change is what matters, and orphans can be reclaimed later with vacuumlo.
DO $$
BEGIN
    PERFORM lo_unlink(t.prompt)
       FROM agent_traces t
       JOIN pg_largeobject_metadata m ON m.oid = t.prompt
      WHERE t.prompt IS NOT NULL;

    PERFORM lo_unlink(t.response)
       FROM agent_traces t
       JOIN pg_largeobject_metadata m ON m.oid = t.response
      WHERE t.response IS NOT NULL;
EXCEPTION
    WHEN insufficient_privilege OR undefined_object THEN
        RAISE NOTICE 'V26: could not unlink large objects (%). Column conversion still applied; '
                     'run vacuumlo to reclaim them.', SQLERRM;
END $$;

ALTER TABLE agent_traces DROP COLUMN prompt;
ALTER TABLE agent_traces DROP COLUMN response;

ALTER TABLE agent_traces RENAME COLUMN prompt_text TO prompt;
ALTER TABLE agent_traces RENAME COLUMN response_text TO response;
