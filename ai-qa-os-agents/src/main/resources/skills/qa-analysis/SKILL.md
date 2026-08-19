---
name: qa-analysis
description: Skill for evaluating User Stories, identifying test scenarios, mapping risk levels, and determining test design readiness.
---

# QA Analysis Skill

Guidelines for QA Analysis execution:

## Scenario Identification
1. Analyze the User Story raw content thoroughly.
2. Identify all testable functional workflows, UI navigation flows, form validations, and error conditions.
3. Formulate scenario titles covering positive flows (valid login, successful navigation), negative flows (invalid email, wrong password, blank fields), edge flows (rapid clicking, tab switching), and security checks (masked fields, injection attempts).

## Risk Matrix Guidelines
- **HIGH**: Authentication bypass, SQL injection, XSS vulnerability, session hijacking, broken navigation to critical recovery pages.
- **MEDIUM**: Form validation failures, unhandled field formats, improper error messages.
- **LOW**: Minor cosmetic issues, non-blocking UI alignment.

## Readiness Gate
- Set `readyForTestDesign: true` only when all acceptance criteria are actionable and testable.
