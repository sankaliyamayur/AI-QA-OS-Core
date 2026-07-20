import { defineConfig, devices } from '@playwright/test';
import path from 'path';

/**
 * AI-QA-OS Playwright Configuration
 *
 * Artifact capture strategy (per AI-QA-OS design):
 *  - screenshot : 'only-on-failure'   → captured only when a test fails
 *  - video      : 'retain-on-failure' → recorded for every test, kept only on failure (saves disk)
 *  - trace      : 'retain-on-failure' → captured for every test, kept only on failure
 *                                        ensures trace exists even on first-run failure (no retry needed)
 *
 * Artifact directory layout:
 *   {PLAYWRIGHT_ARTIFACTS_DIR}/
 *     {executionId}/
 *       {browser}/
 *         test-results/    ← screenshots, videos, traces per test
 *         results.json     ← machine-readable test result manifest
 *         html-report/     ← human-readable HTML report
 */

// Base artifact directory — override via env var in CI/CD
const BASE_ARTIFACTS_DIR = process.env.PLAYWRIGHT_ARTIFACTS_DIR
  ? process.env.PLAYWRIGHT_ARTIFACTS_DIR
  : path.join(process.cwd(), 'playwright-output');

// Execution ID and browser — passed by Java ProcessBuilder or CI pipeline
const EXECUTION_ID = process.env.PLAYWRIGHT_EXECUTION_ID ?? 'local';
const BROWSER_NAME  = process.env.PLAYWRIGHT_BROWSER ?? 'chromium';

// Resolved output directory for this execution run
const OUTPUT_DIR = path.join(BASE_ARTIFACTS_DIR, EXECUTION_ID, BROWSER_NAME);

export default defineConfig({
  // Root directory containing the test spec files
  testDir: './tests',

  // Resolved artifact output directory (browser + executionId scoped)
  outputDir: path.join(OUTPUT_DIR, 'test-results'),

  // Run tests sequentially to keep artifact paths deterministic
  fullyParallel: false,

  // Fail the build immediately if tests.spec files have syntax issues
  forbidOnly: !!process.env.CI,

  // No automatic retries — trace: 'retain-on-failure' ensures trace is captured on first failure
  retries: 0,

  // Single worker in CI to avoid port conflicts on headless browsers
  workers: process.env.CI ? 1 : undefined,

  reporter: [
    // Machine-readable JSON consumed by Java ArtifactManager
    ['json', { outputFile: path.join(OUTPUT_DIR, 'results.json') }],
    // Human-readable HTML report available via dashboard attachment viewer
    ['html', { outputFolder: path.join(OUTPUT_DIR, 'html-report'), open: 'never' }],
    // Always print to stdout for Java ProcessBuilder log capture
    ['line'],
  ],

  use: {
    // ── Screenshot ──────────────────────────────────────────────────────────
    // Capture a full-page screenshot ONLY when the test fails.
    screenshot: 'only-on-failure',

    // ── Video ────────────────────────────────────────────────────────────────
    // Record video for every test execution.
    // Playwright keeps the .webm file only if the test FAILS — passed videos are deleted.
    // This is the most disk-efficient strategy for CI/CD pipelines.
    video: 'retain-on-failure',

    // ── Trace ────────────────────────────────────────────────────────────────
    // Capture a Playwright Trace for every test.
    // Trace is retained ONLY if the test fails (zip file kept, others discarded).
    // Unlike 'on-first-retry', this ensures trace is available even when there is
    // no retry configured — critical for debugging first-run failures.
    trace: 'retain-on-failure',

    // Headless by default; set PLAYWRIGHT_HEADLESS=false for local debugging
    headless: process.env.PLAYWRIGHT_HEADLESS !== 'false',

    // Base URL for navigation calls — override per environment
    baseURL: process.env.APP_BASE_URL ?? 'http://localhost:8080',

    // Standard viewport
    viewport: { width: 1280, height: 720 },

    // Reasonable global timeout per action
    actionTimeout: 30_000,
  },

  // Timeout for the full test (including beforeAll / afterAll hooks)
  timeout: 90_000,

  // Timeout for expect() assertions
  expect: {
    timeout: 15_000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});
