-- V10: Add test_case_id, browser, and run_number columns to execution_artifacts
-- These support:
--   1. Direct dashboard lookup by test case ID (without needing the execution UUID)
--   2. Multi-browser artifact organization (chromium / firefox / webkit)
--   3. Execution history view (Run #1, Run #2, ...) per test case

ALTER TABLE execution_artifacts
    ADD COLUMN IF NOT EXISTS test_case_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS browser      VARCHAR(32),
    ADD COLUMN IF NOT EXISTS run_number   INT DEFAULT 1;

-- Index for fast dashboard lookup: GET /api/dashboard/artifacts/{testCaseId}
CREATE INDEX IF NOT EXISTS idx_exec_artifacts_test_case_id
    ON execution_artifacts(test_case_id);

-- Index for execution history queries ordered by run
CREATE INDEX IF NOT EXISTS idx_exec_artifacts_history
    ON execution_artifacts(test_case_id, run_number ASC);
