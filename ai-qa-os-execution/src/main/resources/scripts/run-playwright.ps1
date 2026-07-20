param (
    [Parameter(Mandatory = $true)]
    [string]$ExecutionId,

    [Parameter(Mandatory = $true)]
    [ValidateSet("chromium", "firefox", "webkit")]
    [string]$Browser,

    [Parameter(Mandatory = $false)]
    [string]$ArtifactsDir = "",

    [Parameter(Mandatory = $false)]
    [string]$AppBaseUrl = "http://localhost:8080",

    [Parameter(Mandatory = $false)]
    [string]$ConfigFile = ""
)

# ============================================================
# run-playwright.ps1
# AI-QA-OS Playwright Runner Script
#
# Usage (invoked by Java PlaywrightExecutionEngine via ProcessBuilder):
#   .\run-playwright.ps1 -ExecutionId "exec-12345" -Browser "chromium" -ArtifactsDir "D:\playwright-output"
#
# Exit codes:
#   0  - All tests passed
#   1  - One or more tests failed
#   2  - Script invocation error (missing params, Node.js not found, etc.)
# ============================================================

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Robustly resolve script root directory
$ScriptRoot = $PSScriptRoot
if ([string]::IsNullOrEmpty($ScriptRoot)) {
    $ScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
}

if ([string]::IsNullOrEmpty($ArtifactsDir)) {
    $ArtifactsDir = Join-Path $ScriptRoot "playwright-output"
}
if ([string]::IsNullOrEmpty($ConfigFile)) {
    $ConfigFile = Join-Path $ScriptRoot "playwright.config.ts"
}

# ── Validate prerequisites ───────────────────────────────────────────────────
Write-Output "[INFO] Checking Node.js and npx availability..."
try {
    $nodeVersion = & node --version 2>&1
    Write-Output "[INFO] Node.js: $nodeVersion"
} catch {
    Write-Error "[ERROR] Node.js not found. Install Node.js >= 18 and ensure it is on PATH."
    exit 2
}

# ── Resolve output directory for this execution/browser combination ───────────
$OutputDir = Join-Path (Join-Path $ArtifactsDir $ExecutionId) $Browser
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
Write-Output "[INFO] Artifact output directory: $OutputDir"

# ── Set environment variables consumed by playwright.config.ts ───────────────
$env:PLAYWRIGHT_ARTIFACTS_DIR  = $ArtifactsDir
$env:PLAYWRIGHT_EXECUTION_ID   = $ExecutionId
$env:PLAYWRIGHT_BROWSER        = $Browser
$env:APP_BASE_URL              = $AppBaseUrl
$env:CI                        = if ($env:CI) { $env:CI } else { "false" }

# Paths that Java will parse from this script's stdout
$ResultsJsonPath = Join-Path $OutputDir "results.json"
$HtmlReportPath  = Join-Path $OutputDir "html-report"

Write-Output "[INFO] Starting Playwright test suite..."
Write-Output "[INFO]   Execution ID : $ExecutionId"
Write-Output "[INFO]   Browser      : $Browser"
Write-Output "[INFO]   App Base URL : $AppBaseUrl"
Write-Output "[INFO]   Config File  : $ConfigFile"

# ── Execute Playwright ────────────────────────────────────────────────────────
$startTime = Get-Date
try {
    $oldErrorAction = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    & npx playwright test `
        --config $ConfigFile `
        --project $Browser `
        2>&1 | ForEach-Object { Write-Output $_ }

    $playwrightExitCode = $LASTEXITCODE
    $ErrorActionPreference = $oldErrorAction
} catch {
    Write-Error "[ERROR] Failed to invoke Playwright: $_"
    exit 2
}

$endTime  = Get-Date
$duration = [math]::Round(($endTime - $startTime).TotalSeconds, 2)

# ── Emit machine-readable manifest consumed by Java ───────────────────────────
# Java ProcessBuilder reads lines prefixed with ARTIFACT_MANIFEST: from stdout
Write-Output "ARTIFACT_MANIFEST:RESULTS_JSON=$ResultsJsonPath"
Write-Output "ARTIFACT_MANIFEST:HTML_REPORT=$HtmlReportPath"
Write-Output "ARTIFACT_MANIFEST:OUTPUT_DIR=$OutputDir"
Write-Output "ARTIFACT_MANIFEST:EXECUTION_ID=$ExecutionId"
Write-Output "ARTIFACT_MANIFEST:BROWSER=$Browser"
Write-Output "ARTIFACT_MANIFEST:DURATION_SEC=$duration"
Write-Output "ARTIFACT_MANIFEST:EXIT_CODE=$playwrightExitCode"

if ($playwrightExitCode -eq 0) {
    Write-Output "[INFO] All Playwright tests PASSED in ${duration}s."
} else {
    Write-Output "[WARN] Playwright tests FAILED (exit code $playwrightExitCode) in ${duration}s."
    Write-Output "[WARN] Artifacts saved for failed tests in: $OutputDir"
}

exit $playwrightExitCode
