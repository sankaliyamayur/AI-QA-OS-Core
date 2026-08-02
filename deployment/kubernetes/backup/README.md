# AI-QA-OS — Backup & DR CronJobs (ENT-5 / FI-ENT5-B, ADR-074)

Nightly backups of the platform's durable state to object storage (MinIO or any S3-compatible store,
incl. real AWS S3). These were placeholder templates; they are now runnable manifests that upload for
real, with **no plaintext credentials** — everything sensitive comes from the `aiqaos-backup-secret`
Secret, created out-of-band.

## What gets backed up

| CronJob | Schedule | What | Destination |
|---|---|---|---|
| `postgres-backup` | 02:00 daily | `pg_dump -Fc` of the platform DB | `$BACKUP_BUCKET/postgres/` |
| `qdrant-backup`   | 02:30 daily | Qdrant full-storage snapshot | `$BACKUP_BUCKET/qdrant/` |
| `artifacts-backup`| 03:00 daily | on-disk artifact tree (legacy local-PVC deployments) | `$BACKUP_BUCKET/artifacts/` |

> With the object-storage `ArtifactStore` primary (`aiqaos.artifacts.store=object` + upload on,
> FI-ENT5-A/ADR-071), execution artifacts already land in object storage — so `artifacts-backup` is
> only needed for `store=local` deployments.

## 1. Create the Secret (never committed — SEC-2)

```bash
kubectl -n ai-qa-os create secret generic aiqaos-backup-secret \
  --from-literal=AWS_ACCESS_KEY_ID=<key> \
  --from-literal=AWS_SECRET_ACCESS_KEY=<secret> \
  --from-literal=S3_ENDPOINT=http://minio-service:9000 \   # set "" for real AWS S3
  --from-literal=BACKUP_BUCKET=s3://aiqaos-backups \
  --from-literal=POSTGRES_PASSWORD=<db-password>
```
`backup-secret.yaml` documents the shape; prefer sealed-secrets / external-secrets in production.

## 2. Adjust to your deployment

- Postgres: `POSTGRES_HOST` / `POSTGRES_DB` / `POSTGRES_USER` in `postgres-backup.yaml` must match `databases/postgres.yaml`.
- Qdrant: `QDRANT_URL` in `qdrant-backup.yaml` must match `databases/qdrant.yaml`.
- Artifacts: `artifacts-pvc` must match the execution artifact volume.
- The backup **bucket must already exist** (the compose stack pre-creates `aiqaos-backups`; create it in prod).

## 3. Apply

```bash
kubectl apply -k deployment/kubernetes/backup            # after the Secret exists
# smoke-test one now instead of waiting for the schedule:
kubectl -n ai-qa-os create job --from=cronjob/postgres-backup postgres-backup-manual
kubectl -n ai-qa-os logs job/postgres-backup-manual -f
```
> Author's note: these manifests are authored and YAML-validated but were **not applied to a live
> cluster** here (no cluster in the build env). Run `kubectl apply --dry-run=server -k .` and one
> manual job before trusting the schedule.

## 4. Restore (outline)

- **Postgres:** `aws s3 cp $BACKUP_BUCKET/postgres/pg-<TS>.dump .` then `pg_restore -h <host> -U <user> -d <db> --clean pg-<TS>.dump`.
- **Qdrant:** download the snapshot, then `PUT /snapshots/upload` (or place it in the snapshots dir) and recover per the Qdrant docs.
- **Artifacts:** `aws s3 sync $BACKUP_BUCKET/artifacts/ /playwright-output` (local-store), or they are already durable in the primary object store.

## Retention

Backups: apply a **bucket lifecycle rule** (expire objects older than N days) on the backup bucket.
Live artifacts: `ArtifactRetentionService` (in-app, opt-in via `aiqaos.artifacts.retention.enabled`)
age-purges the artifact tree; wiring it to a scheduled trigger is a follow-on (FI-ENT5-F).
