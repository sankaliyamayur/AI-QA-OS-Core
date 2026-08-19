---
name: bug-analysis
description: Skill for diagnosing test execution failures, identifying root causes, and categorizing bug reports.
---

# Bug Analysis Skill

Guidelines for failure diagnosis:

## Failure Categorization
- `ELEMENT_NOT_FOUND`: Selector drift or element missing from DOM.
- `ASSERTION_ERROR`: Expected state does not match actual state.
- `TIMEOUT`: Page load or API response timeout.
- `NETWORK_ERROR`: 4xx/5xx HTTP responses or connection drop.
- `UNKNOWN`: Unclassified execution exception.

## Root Cause Analysis
1. Inspect trace logs and stack traces to isolate exact failure line.
2. Formulate `selfHealingSuggestion` (e.g. "Update locator from #old-btn to [data-testid='new-btn']").
3. Set `requiresRegeneration: true` only if script structure is fundamentally flawed.
