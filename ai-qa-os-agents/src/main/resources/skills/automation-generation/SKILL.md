---
name: automation-generation
description: Skill for generating Playwright TypeScript/JavaScript automation scripts from structured test case suites.
---

# Automation Script Generation Skill

Guidelines for generating automation scripts:

## Framework Standards
- **Framework**: Playwright
- **Language**: JAVASCRIPT / TYPESCRIPT
- **Target Platform**: WEB

## Script Writing Rules
1. Every generated script object must reference its corresponding `testCaseId` (e.g., `TC-1`).
2. Code must use async/await with Playwright API (`page.goto()`, `page.click()`, `page.fill()`, `expect()`).
3. Handle dynamic waits gracefully (`await expect(locator).toBeVisible()`).
4. Avoid static delays (`page.waitForTimeout`).
5. Ensure scripts capture clean assertions for both positive and negative validations.
