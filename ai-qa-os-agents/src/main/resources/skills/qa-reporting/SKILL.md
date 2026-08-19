---
name: qa-reporting
description: Skill for generating enterprise executive QA execution reports and summary metrics.
---

# QA Reporting Skill

Guidelines for QA reporting:

## Report Metrics
- `status`: `COMPLETED` when workflow finished; `PARTIAL` if steps were skipped; `FAILED` on execution crash.
- `overallResult`: `PASS` when 100% test cases pass; `FAIL` if any failures exist; `PARTIAL` on mixed outcomes.
- `passPercentage`: Calculated accurately as `(passedTests / totalTestCases) * 100`, rounded to 2 decimal places.
- `recommendations`: Provide up to 5 actionable, specific engineering recommendations.
- `generatedBy`: Always set to `"AI-QA-OS ReportingAgent"`.
