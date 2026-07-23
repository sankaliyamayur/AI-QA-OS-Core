# Operational Smoke-Test Scripts

Manual developer smoke tests for a **locally running** AI-QA-OS (gateway on `:8082`, dashboard on `:8090`).
Moved here from the repo root by ORG-4. These are convenience scripts — not part of the build, CI, or product.

> **Note (SEC-1):** authentication is now enforced when `aiqaos.security.enabled=true`. Scripts that make
> unauthenticated calls will receive `401` in enforced environments — obtain a token first (see `check-api.ps1`),
> or run against a profile with security disabled.

| Script | What it does |
|---|---|
| `trigger.ps1` | Kicks off the autonomous QA pipeline — `POST /api/v1/workflows/start` on the gateway (`:8082`). Primary end-to-end entry point. |
| `request.json` | Sample `workflows/start` request payload (companion to `trigger.ps1`). |
| `check-api.ps1` | Logs in (`admin`/`admin`), then fetches artifacts + history + a raw screenshot with a `Bearer` token. |
| `verify-all.ps1` | Four checks: artifact JSON for `TC-AL-003`, screenshot fetch, video fetch, and a login token. |
| `verify-artifacts.ps1` | Fetches artifact JSON, then follows `screenshotUrl` and `videoUrl`, printing content types/sizes. |
| `test-artifacts.ps1` | Unauthenticated GETs of two artifact endpoints and `/actuator/health` (gitignored). |
| `test-login.ps1` | Single login `POST` with `admin`/`admin` (gitignored). |

Run from this directory, e.g.:

```powershell
./trigger.ps1
```

> Known limitations (tracked as FI-ORG4-B): the scripts hardcode `localhost` ports and IDs like `TC-AL-003` /
> `exec-841076ca`, and `verify-all.ps1` prints a PASSED summary unconditionally — read the per-check output.
