-- AI-QA-OS local native-Postgres setup (idempotent).
-- Creates the app role + database matching the gateway `compose` profile defaults
-- (qaosuser / qaoslocaldev / ai_qa_os_dashboard), so no app config change is needed.
-- Run as the postgres superuser:
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -f deployment/local/setup-postgres.sql
-- (psql will prompt for the postgres password.)

-- 1. Role (create only if absent) — LOGIN role the app connects as.
SELECT format('CREATE ROLE qaosuser LOGIN PASSWORD %L', 'qaoslocaldev')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'qaosuser')\gexec

-- 2. Database (create only if absent) — owned by qaosuser.
SELECT 'CREATE DATABASE ai_qa_os_dashboard OWNER qaosuser'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'ai_qa_os_dashboard')\gexec

-- 3. Privileges (database-level).
GRANT ALL PRIVILEGES ON DATABASE ai_qa_os_dashboard TO qaosuser;

-- 4. Schema privileges. Postgres 15+ no longer grants CREATE on schema public by default,
--    and the database owner is NOT automatically the public-schema owner — so Flyway (running
--    as qaosuser) would hit "permission denied for schema public". Make qaosuser own it.
\connect ai_qa_os_dashboard
ALTER SCHEMA public OWNER TO qaosuser;
GRANT ALL ON SCHEMA public TO qaosuser;

-- 5. Confirm.
\echo '--- roles ---'
SELECT rolname FROM pg_roles WHERE rolname = 'qaosuser';
\echo '--- databases ---'
SELECT datname FROM pg_database WHERE datname = 'ai_qa_os_dashboard';
\echo '--- public schema owner (should be qaosuser) ---'
SELECT nspname, pg_get_userbyid(nspowner) AS owner FROM pg_namespace WHERE nspname = 'public';
