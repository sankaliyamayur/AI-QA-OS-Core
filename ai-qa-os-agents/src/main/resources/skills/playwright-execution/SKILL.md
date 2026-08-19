---
name: playwright-execution
description: Skill for configuring execution strategy, environment parameters, timeouts, and parallelization for Playwright runs.
---

# Playwright Execution Strategy Skill

Guidelines for test execution planning:

## Strategy Configuration
1. Default `executionMode`: `SEQUENTIAL`
2. Default `browser`: `CHROME`
3. Default `headless`: `true`
4. Default `timeout`: `30000` (ms)
5. Default `retryCount`: `2`
6. `environment`: `DEV` / `QA`
