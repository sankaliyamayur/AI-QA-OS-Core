---
name: self-healing
description: Skill for evaluating bug reports and selecting appropriate auto-healing recovery actions (LOCATOR_UPDATE, SCRIPT_REGENERATE, etc.).
---

# Self-Healing Skill

Guidelines for self-healing actions:

## Healing Action Selection
- `LOCATOR_UPDATE`: Select when failure category is `ELEMENT_NOT_FOUND` or selector drifted.
- `SCRIPT_REGENERATE`: Select when `requiresRegeneration: true` or script logic is invalid.
- `WAIT_STRATEGY_CHANGE`: Select when failure is `TIMEOUT` without missing element.
- `TEST_DATA_UPDATE`: Select when failure is due to expired or missing test data.
- `RETRY_ONLY`: Select for transient network glitches.

## Verification Gate
- Require confidence score >= 0.85 before recommending `SCRIPT_REGENERATE`.
