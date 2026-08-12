-- V27: the remaining PostgreSQL large object (oid) columns back to TEXT.
--
-- Completes what V26 started for agent_traces. V6 converted several @Lob String columns from TEXT to
-- OID so Hibernate schema validation would pass, because @Lob String maps to oid under the
-- PostgreSQL dialect. That made the schema agree with the mapping at the cost of the data: a large
-- object can only be read inside a transaction, so any read outside one fails with
--
--   PSQLException: Large Objects may not be used in auto-commit mode.
--
-- and a large object outlives its row, so every delete leaks one. An audit of a live database found
-- 631 large objects, 124 of them already orphaned.
--
-- Of the five columns here, only prompt_versions.content had a live read path (DbPromptSource), and
-- it had not failed only because the table is empty — prompts load from the classpath. The rest were
-- write-only, leaking quietly.
--
-- Two shapes are used, for a reason:
--
--   observability_events.payload holds data (507 rows in the audited database) and is NULLABLE, so it
--   uses the V26 five-step: copy each value out with lo_get(), unlink the objects, then swap columns.
--
--   The other four are NOT NULL. A five-step would silently drop that constraint unless it were
--   re-added; ALTER ... TYPE preserves NOT NULL and column position automatically. They are empty in
--   the audited database, so the USING expression never executes there — but it is written to convert
--   data correctly anyway rather than assume emptiness, since another environment may differ.
--
--   Caveat for that case: ALTER ... TYPE cannot capture the old oids, so where those tables DO hold
--   rows their large objects are orphaned rather than unlinked (reclaim with vacuumlo). The notice
--   below leaves a breadcrumb when that happens. A dangling oid there fails loudly rather than
--   losing data. PostgreSQL forbids subqueries in USING, which is why the EXISTS guard used for
--   observability_events cannot be reused here.

-- ── observability_events.payload — has data, nullable ────────────────────────────────────────────

ALTER TABLE observability_events ADD COLUMN payload_text TEXT;

-- The EXISTS guard skips oids whose large object is already gone; without it one dangling reference
-- would abort the whole migration on lo_get().
UPDATE observability_events
   SET payload_text = convert_from(lo_get(payload), 'UTF8')
 WHERE payload IS NOT NULL
   AND EXISTS (SELECT 1 FROM pg_largeobject_metadata m WHERE m.oid = observability_events.payload);

-- Dropping an oid column does not release what it points to, so unlink before the swap or the
-- objects are orphaned permanently.
DO $$
BEGIN
    PERFORM lo_unlink(t.payload)
       FROM observability_events t
       JOIN pg_largeobject_metadata m ON m.oid = t.payload
      WHERE t.payload IS NOT NULL;
EXCEPTION
    WHEN insufficient_privilege OR undefined_object THEN
        RAISE NOTICE 'V27: could not unlink observability_events large objects (%). Column '
                     'conversion still applied; run vacuumlo to reclaim them.', SQLERRM;
END $$;

ALTER TABLE observability_events DROP COLUMN payload;
ALTER TABLE observability_events RENAME COLUMN payload_text TO payload;

-- ── the four NOT NULL columns — empty in the audited database ────────────────────────────────────

DO $$
DECLARE
    n bigint;
BEGIN
    SELECT (SELECT count(*) FROM agent_executions)
         + (SELECT count(*) FROM prompt_versions)
         + (SELECT count(*) FROM memory_nodes)
      INTO n;
    IF n > 0 THEN
        RAISE NOTICE 'V27: agent_executions/prompt_versions/memory_nodes hold % row(s). Their values '
                     'are converted, but the old large objects are orphaned rather than unlinked — '
                     'run vacuumlo to reclaim them.', n;
    END IF;
END $$;

ALTER TABLE agent_executions
    ALTER COLUMN request_payload TYPE text USING convert_from(lo_get(request_payload), 'UTF8');

ALTER TABLE agent_executions
    ALTER COLUMN response_payload TYPE text USING convert_from(lo_get(response_payload), 'UTF8');

ALTER TABLE prompt_versions
    ALTER COLUMN content TYPE text USING convert_from(lo_get(content), 'UTF8');

ALTER TABLE memory_nodes
    ALTER COLUMN content TYPE text USING convert_from(lo_get(content), 'UTF8');
