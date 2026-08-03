-- AI-QA-OS local Postgres RESET (DESTRUCTIVE — drops the app database).
-- Use when a pre-existing ai_qa_os_dashboard has objects owned by another role (e.g. a stale
-- flyway_schema_history -> "permission denied"). Recreates it clean, fully owned by qaosuser, so
-- Flyway starts from a fresh baseline. There is no real data in a local dev DB.
-- Run as the postgres superuser (connected to the default 'postgres' database):
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d postgres -f deployment/local/reset-postgres.sql

-- Ensure the app role exists (idempotent).
SELECT format('CREATE ROLE qaosuser LOGIN PASSWORD %L', 'qaoslocaldev')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'qaosuser')\gexec

-- Terminate any lingering connections to the database, then drop + recreate it.
SELECT pg_terminate_backend(pid) FROM pg_stat_activity
WHERE datname = 'ai_qa_os_dashboard' AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS ai_qa_os_dashboard;
CREATE DATABASE ai_qa_os_dashboard OWNER qaosuser;

-- Make qaosuser own the public schema (PG15+), so Flyway can create tables.
\connect ai_qa_os_dashboard
ALTER SCHEMA public OWNER TO qaosuser;
GRANT ALL ON SCHEMA public TO qaosuser;

\echo '--- public schema owner (should be qaosuser) ---'
SELECT nspname, pg_get_userbyid(nspowner) AS owner FROM pg_namespace WHERE nspname = 'public';
\echo '--- objects in public (should be empty) ---'
SELECT count(*) AS objects FROM information_schema.tables WHERE table_schema = 'public';
