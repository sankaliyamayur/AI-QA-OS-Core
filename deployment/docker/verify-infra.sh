#!/usr/bin/env bash
#
# AI-QA-OS Phase 1 (T10) — one-shot local-infrastructure health verifier.
#
# Proves the docker-compose stack is up and reachable:
#   REQUIRED : postgres, redis, kafka, minio, qdrant
#   INFO     : Flyway schema version (needs the gateway to have run), MinIO buckets
#
# Note: the Qdrant / Kafka / MinIO *Java bindings* are deferred to their consuming features
# (SCALE-3 / SCALE-2 / ENT-5). Phase 1 provisions the CONTAINERS, so this script verifies the
# whole stack is up and ready for when those bindings land — it does NOT require the apps.
#
# Usage:
#   ./verify-infra.sh          verify services that are already running (fast fail)
#   ./verify-infra.sh --up     `docker compose up -d` first, then verify (waits for cold start)
#   ./verify-infra.sh --down   tear the stack down and exit
#
# Exit 0 = all REQUIRED services healthy; non-zero = at least one failed.
#
# Windows: run from Git Bash / WSL (`bash verify-infra.sh`). Needs `docker` and `curl` on PATH.

set -uo pipefail
cd "$(dirname "$0")"                        # always run from deployment/docker (compose lives here)

# --- dev defaults mirror docker-compose.yml; a local .env overrides them ---
if [ -f .env ]; then set -a; . ./.env; set +a; fi
POSTGRES_USER="${POSTGRES_USER:-qaosuser}"
POSTGRES_DB="${POSTGRES_DB:-ai_qa_os_dashboard}"
MINIO_ROOT_USER="${MINIO_ROOT_USER:-minioadmin}"
MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-minioadmin}"

DC="docker compose"
FAILED=0
RETRIES=3; DELAY=3                          # fast fail for "already running"

pass() { printf '  \033[32mPASS\033[0m  %s\n' "$1"; }
fail() { printf '  \033[31mFAIL\033[0m  %s\n' "$1"; FAILED=$((FAILED+1)); }
info() { printf '  \033[36mINFO\033[0m  %s\n' "$1"; }
hdr()  { printf '\n== %s ==\n' "$1"; }

# retry <cmd...> up to $RETRIES times, sleeping $DELAY between attempts
retry() {
  local n="$RETRIES"
  while [ "$n" -gt 0 ]; do
    if "$@" >/dev/null 2>&1; then return 0; fi
    n=$((n - 1)); [ "$n" -gt 0 ] && sleep "$DELAY"
  done
  return 1
}

case "${1:-}" in
  --down)
    hdr "Tearing down"; exec $DC down ;;
  --up)
    hdr "Bringing up infra (docker compose up -d)"
    $DC up -d || { echo "docker compose up failed"; exit 1; }
    RETRIES=18; DELAY=5                      # allow ~90s per service for cold start
    info "waiting for healthchecks (up to ~90s per service)…" ;;
  "" ) : ;;
  * ) echo "unknown option: $1 (use --up | --down | no arg)"; exit 2 ;;
esac

# --- probes (each returns 0/1 so retry() can wrap it) ---
p_postgres() { $DC exec -T postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"; }
p_redis()    { [ "$($DC exec -T redis redis-cli ping 2>/dev/null | tr -d '\r\n')" = "PONG" ]; }
p_kafka()    { $DC exec -T kafka /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092; }
p_qdrant()   { curl -fsS http://localhost:6333/healthz; }
p_minio()    { curl -fsS http://localhost:9000/minio/health/live; }

hdr "Required services"
retry p_postgres && pass "postgres — accepting connections (db=$POSTGRES_DB)" || fail "postgres — pg_isready failed"
retry p_redis    && pass "redis — PONG"                                       || fail "redis — no PONG"
retry p_kafka    && pass "kafka — broker responding on :9092"                 || fail "kafka — broker not responding"
retry p_qdrant   && pass "qdrant — /healthz ok (:6333)"                       || fail "qdrant — /healthz unreachable (curl on PATH? port 6333 published?)"
retry p_minio    && pass "minio — /health/live ok (:9000)"                    || fail "minio — /health/live unreachable"

hdr "Informational (depend on app/init having run)"
# Flyway schema version — only present after the gateway has booted on the 'compose' profile once.
FLYWAY="$($DC exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc \
  "select coalesce(max(version::int)::text,'-') from flyway_schema_history where success" 2>/dev/null | tr -d '\r\n ')"
if [ -n "$FLYWAY" ] && [ "$FLYWAY" != "-" ]; then
  info "flyway — schema at V$FLYWAY (gateway migrations applied)"
else
  info "flyway — no schema_history yet; boot the gateway on the 'compose' profile to migrate V1..V16"
fi

# MinIO buckets — best-effort via a throwaway mc container on the compose network.
BUCKETS="$($DC run --rm -T --entrypoint sh minio-createbuckets -c \
  "mc alias set local http://minio:9000 '$MINIO_ROOT_USER' '$MINIO_ROOT_PASSWORD' >/dev/null 2>&1 && mc ls local" \
  2>/dev/null || true)"
if echo "$BUCKETS" | grep -q "aiqaos-artifacts" && echo "$BUCKETS" | grep -q "aiqaos-backups"; then
  info "minio buckets — aiqaos-artifacts, aiqaos-backups present"
else
  info "minio buckets — not confirmed; run '$DC up -d minio-createbuckets' to (re)create them"
fi

hdr "Result"
if [ "$FAILED" -eq 0 ]; then
  printf '  \033[32mAll required infra healthy.\033[0m\n'; exit 0
else
  printf '  \033[31m%d required check(s) failed.\033[0m See above.\n' "$FAILED"; exit 1
fi
